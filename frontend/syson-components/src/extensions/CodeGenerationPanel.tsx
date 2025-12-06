/*******************************************************************************
 * Copyright (c) 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/

import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Grid,
    Paper,
    Tab,
    Tabs,
    TextField,
    Typography,
} from '@mui/material';
import { useState } from 'react';
import { AIChatPanel } from './AIChatPanel';
import { CodePreview } from './CodePreview';

export interface CodeGenerationPanelProps {
  /** Current model context */
  modelContext?: {
    modelId?: string;
    elementType?: string;
    elementName?: string;
    currentCode?: string;
  };
  /** Project ID for imports */
  projectId?: string;
  /** API base URL */
  apiBaseUrl?: string;
  /** Called when code is successfully imported */
  onCodeImported?: (modelId: string) => void;
}

interface GenerationHistory {
  id: string;
  description: string;
  code: string;
  timestamp: Date;
}

/**
 * Code Generation Panel Component
 * 
 * Main panel for AI-powered code generation with chat and code preview.
 * Supports both natural language generation and chat assistance.
 */
export const CodeGenerationPanel = ({
  modelContext,
  projectId,
  apiBaseUrl = '/api/ai',
  onCodeImported,
}: CodeGenerationPanelProps) => {
  const [mode, setMode] = useState<'chat' | 'generate'>('generate');
  const [description, setDescription] = useState('');
  const [generatedCode, setGeneratedCode] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResult, setValidationResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<GenerationHistory[]>([]);

  const handleGenerate = async () => {
    if (!description.trim()) return;

    setIsGenerating(true);
    setError(null);
    setGeneratedCode('');
    setValidationResult(null);

    try {
      const response = await fetch(`${apiBaseUrl}/code/generate/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          description,
          context: modelContext || {},
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // Handle streaming response
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let code = '';

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value);
          const lines = chunk.split('\n');

          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const data = line.substring(6);
              code += data;
              setGeneratedCode(code);
            }
          }
        }
      }

      // Add to history
      const historyItem: GenerationHistory = {
        id: `gen-${Date.now()}`,
        description,
        code,
        timestamp: new Date(),
      };
      setHistory((prev) => [historyItem, ...prev].slice(0, 10)); // Keep last 10

      // Auto-validate generated code
      handleValidate(code);
    } catch (err) {
      console.error('Generation error:', err);
      setError(err instanceof Error ? err.message : 'Unknown error occurred');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleValidate = async (code: string) => {
    if (!code.trim()) return;

    setIsValidating(true);
    setValidationResult(null);

    try {
      const response = await fetch(`${apiBaseUrl}/code/validate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ code }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      setValidationResult(result);
    } catch (err) {
      console.error('Validation error:', err);
      setError(`Validation failed: ${err instanceof Error ? err.message : 'Unknown error'}`);
    } finally {
      setIsValidating(false);
    }
  };

  const handleImport = async (code: string) => {
    if (!projectId) {
      setError('No project selected for import');
      return;
    }

    try {
      const response = await fetch(`${apiBaseUrl}/code/import`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code,
          projectId,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();

      if (result.success) {
        if (onCodeImported) {
          onCodeImported(result.modelId);
        }
        setError(null);
        // Show success message or notification
      } else {
        setError(result.message || 'Import failed');
      }
    } catch (err) {
      console.error('Import error:', err);
      setError(`Import failed: ${err instanceof Error ? err.message : 'Unknown error'}`);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && e.ctrlKey) {
      handleGenerate();
    }
  };

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Mode Tabs */}
      <Paper square sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={mode} onChange={(_, newValue) => setMode(newValue)}>
          <Tab
            label="Code Generation"
            value="generate"
            icon={<AutoAwesomeIcon />}
            iconPosition="start"
          />
          <Tab label="AI Chat" value="chat" />
        </Tabs>
      </Paper>

      {/* Content */}
      <Box sx={{ flex: 1, overflow: 'hidden', p: 2 }}>
        {mode === 'chat' ? (
          <AIChatPanel modelContext={modelContext} apiBaseUrl={apiBaseUrl} />
        ) : (
          <Grid container spacing={2} sx={{ height: '100%' }}>
            {/* Generation Input */}
            <Grid item xs={12} md={6} sx={{ height: '100%' }}>
              <Paper sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
                <Typography variant="h6" gutterBottom>
                  Describe Your Model
                </Typography>

                <TextField
                  fullWidth
                  multiline
                  rows={8}
                  placeholder="Example: Create a vehicle system with an engine (500 kW power), a transmission (automatic), and 4 wheels. The engine connects to the transmission."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  onKeyPress={handleKeyPress}
                  variant="outlined"
                  sx={{ mb: 2 }}
                  helperText="Press Ctrl+Enter to generate"
                />

                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  onClick={handleGenerate}
                  disabled={!description.trim() || isGenerating}
                  startIcon={isGenerating ? <CircularProgress size={20} /> : <AutoAwesomeIcon />}
                >
                  {isGenerating ? 'Generating...' : 'Generate SysML Code'}
                </Button>

                {error && (
                  <Alert severity="error" sx={{ mt: 2 }}>
                    {error}
                  </Alert>
                )}

                {/* Generation History */}
                {history.length > 0 && (
                  <Box sx={{ mt: 3 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Recent Generations
                    </Typography>
                    <Box sx={{ maxHeight: '200px', overflow: 'auto' }}>
                      {history.map((item) => (
                        <Paper
                          key={item.id}
                          variant="outlined"
                          sx={{
                            p: 1.5,
                            mb: 1,
                            cursor: 'pointer',
                            '&:hover': { bgcolor: 'action.hover' },
                          }}
                          onClick={() => {
                            setDescription(item.description);
                            setGeneratedCode(item.code);
                          }}
                        >
                          <Typography variant="body2" noWrap>
                            {item.description}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {item.timestamp.toLocaleString()}
                          </Typography>
                        </Paper>
                      ))}
                    </Box>
                  </Box>
                )}
              </Paper>
            </Grid>

            {/* Code Preview */}
            <Grid item xs={12} md={6} sx={{ height: '100%' }}>
              {generatedCode ? (
                <CodePreview
                  code={generatedCode}
                  validationResult={validationResult}
                  isValidating={isValidating}
                  onImport={handleImport}
                  onValidate={handleValidate}
                  projectId={projectId}
                />
              ) : (
                <Paper
                  sx={{
                    height: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'text.secondary',
                  }}
                >
                  <Box sx={{ textAlign: 'center' }}>
                    <AutoAwesomeIcon sx={{ fontSize: 60, opacity: 0.3, mb: 2 }} />
                    <Typography variant="body1">
                      Generated code will appear here
                    </Typography>
                  </Box>
                </Paper>
              )}
            </Grid>
          </Grid>
        )}
      </Box>
    </Box>
  );
};
