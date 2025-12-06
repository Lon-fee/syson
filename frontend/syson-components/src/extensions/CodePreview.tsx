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

import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import DownloadIcon from '@mui/icons-material/Download';
import ErrorIcon from '@mui/icons-material/Error';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import WarningIcon from '@mui/icons-material/Warning';
import {
    Alert,
    Box,
    Button,
    Chip,
    Divider,
    IconButton,
    Paper,
    Tooltip,
    Typography,
} from '@mui/material';
import { useState } from 'react';

export interface ValidationIssue {
  severity: 'ERROR' | 'WARNING' | 'INFO';
  message: string;
  suggestion?: string;
  line?: number;
}

export interface CodePreviewProps {
  /** SysML V2 code to display */
  code: string;
  /** Validation results */
  validationResult?: {
    valid: boolean;
    hasErrors: boolean;
    hasWarnings: boolean;
    errors: ValidationIssue[];
    warnings: ValidationIssue[];
    allIssues: ValidationIssue[];
  };
  /** Whether validation is in progress */
  isValidating?: boolean;
  /** Called when user wants to import code */
  onImport?: (code: string) => void;
  /** Called when user wants to validate code */
  onValidate?: (code: string) => void;
  /** Project ID for import */
  projectId?: string;
}

/**
 * Code Preview Component
 * 
 * Displays generated SysML V2 code with syntax highlighting,
 * validation results, and import capabilities.
 */
export const CodePreview = ({
  code,
  validationResult,
  isValidating = false,
  onImport,
  onValidate,
  projectId,
}: CodePreviewProps) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  const handleDownload = () => {
    const blob = new Blob([code], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'generated-model.sysml';
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImport = () => {
    if (onImport && (!validationResult || validationResult.valid)) {
      onImport(code);
    }
  };

  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'ERROR':
        return <ErrorIcon color="error" fontSize="small" />;
      case 'WARNING':
        return <WarningIcon color="warning" fontSize="small" />;
      default:
        return <CheckCircleIcon color="info" fontSize="small" />;
    }
  };

  return (
    <Paper sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Box
        sx={{
          p: 2,
          borderBottom: 1,
          borderColor: 'divider',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h6">Generated Code</Typography>
          {validationResult && (
            <Chip
              icon={validationResult.valid ? <CheckCircleIcon /> : <ErrorIcon />}
              label={validationResult.valid ? 'Valid' : 'Has Issues'}
              color={validationResult.valid ? 'success' : 'error'}
              size="small"
            />
          )}
        </Box>

        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title={copied ? 'Copied!' : 'Copy code'}>
            <IconButton size="small" onClick={handleCopy}>
              {copied ? <CheckCircleIcon color="success" /> : <ContentCopyIcon />}
            </IconButton>
          </Tooltip>

          <Tooltip title="Download code">
            <IconButton size="small" onClick={handleDownload}>
              <DownloadIcon />
            </IconButton>
          </Tooltip>

          {onValidate && (
            <Button
              variant="outlined"
              size="small"
              onClick={() => onValidate(code)}
              disabled={isValidating}
              startIcon={<PlayArrowIcon />}
            >
              {isValidating ? 'Validating...' : 'Validate'}
            </Button>
          )}

          {onImport && (
            <Button
              variant="contained"
              size="small"
              onClick={handleImport}
              disabled={validationResult ? !validationResult.valid : false}
              startIcon={<DownloadIcon />}
            >
              Import
            </Button>
          )}
        </Box>
      </Box>

      {/* Code Display */}
      <Box
        sx={{
          flex: 1,
          overflow: 'auto',
          bgcolor: 'grey.50',
          p: 2,
        }}
      >
        <Box
          component="pre"
          sx={{
            m: 0,
            fontFamily: 'monospace',
            fontSize: '0.875rem',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
          }}
        >
          <code>{code}</code>
        </Box>
      </Box>

      {/* Validation Results */}
      {validationResult && validationResult.allIssues.length > 0 && (
        <Box
          sx={{
            maxHeight: '200px',
            overflow: 'auto',
            borderTop: 1,
            borderColor: 'divider',
            p: 2,
          }}
        >
          <Typography variant="subtitle2" gutterBottom>
            Validation Issues ({validationResult.allIssues.length})
          </Typography>

          <Divider sx={{ my: 1 }} />

          {validationResult.allIssues.map((issue, index) => (
            <Alert
              key={index}
              severity={issue.severity.toLowerCase() as 'error' | 'warning' | 'info'}
              icon={getSeverityIcon(issue.severity)}
              sx={{ mb: 1, fontSize: '0.875rem' }}
            >
              <Box>
                <Typography variant="body2" fontWeight="medium">
                  {issue.message}
                  {issue.line && (
                    <Chip
                      label={`Line ${issue.line}`}
                      size="small"
                      sx={{ ml: 1, height: 20 }}
                    />
                  )}
                </Typography>
                {issue.suggestion && (
                  <Typography variant="caption" color="text.secondary" display="block" mt={0.5}>
                    💡 {issue.suggestion}
                  </Typography>
                )}
              </Box>
            </Alert>
          ))}
        </Box>
      )}

      {/* Stats */}
      <Box
        sx={{
          p: 1.5,
          borderTop: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="caption" color="text.secondary">
          {code.split('\n').length} lines · {code.length} characters
        </Typography>

        {validationResult && (
          <Box sx={{ display: 'flex', gap: 2 }}>
            {validationResult.hasErrors && (
              <Typography variant="caption" color="error">
                {validationResult.errors.length} errors
              </Typography>
            )}
            {validationResult.hasWarnings && (
              <Typography variant="caption" color="warning.main">
                {validationResult.warnings.length} warnings
              </Typography>
            )}
          </Box>
        )}
      </Box>
    </Paper>
  );
};
