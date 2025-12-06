# SysON AI Services

AI-powered assistance for SysML V2 modeling in Eclipse SysON.

## Features

- **AI Chat**: Context-aware chat with SysML V2 expert
- **Code Generation**: Generate SysML V2 code from natural language
- **Model Analysis**: Analyze and provide insights on model elements
- **Validation**: Validate SysML V2 code syntax and semantics
- **Suggestions**: Get intelligent modeling suggestions

## Supported AI Providers

- **Google Gemini** (Default, Recommended)
  - gemini-2.0-flash-exp (Fast, long context)
  - gemini-1.5-pro (High quality)
  
- **OpenAI** (Alternative)
  - gpt-4
  - gpt-3.5-turbo

## Quick Start

### 1. Get API Key

**For Google Gemini:**
1. Visit https://ai.google.dev/
2. Sign in with Google account
3. Create API key
4. Copy the API key

### 2. Configure

Add to `application.properties` or set environment variable:

```properties
syson.ai.gemini.api-key=YOUR_GEMINI_API_KEY
```

Or set environment variable:
```bash
export GEMINI_API_KEY=your-api-key-here
```

### 3. Enable AI Services

```properties
syson.ai.enabled=true
syson.ai.provider=gemini
```

### 4. Start Application

```bash
cd backend/application/syson-application
mvn spring-boot:run
```

## API Endpoints

### Chat

```bash
# Regular chat
POST /api/ai/chat
{
  "message": "What is a part definition in SysML V2?",
  "context": {
    "modelId": "123",
    "elementType": "PartDefinition"
  }
}

# Streaming chat
POST /api/ai/chat/stream
# Returns Server-Sent Events
```

### Code Generation

```bash
# Generate code
POST /api/ai/code/generate
{
  "description": "Create a vehicle system with engine and wheels",
  "context": {
    "currentCode": "package AutomotiveSystems { ... }"
  }
}

# Streaming code generation  
POST /api/ai/code/generate/stream
# Returns Server-Sent Events with code chunks
```

### Validation

```bash
POST /api/ai/validate
{
  "code": "part def Engine { attribute power : Real; }"
}

# Response:
{
  "valid": true,
  "issues": []
}
```

### Analysis

```bash
POST /api/ai/analyze
{
  "content": "part def Vehicle { ... }",
  "elementType": "PartDefinition"
}
```

### Suggestions

```bash
POST /api/ai/suggestions
{
  "context": {
    "modelId": "123",
    "elementType": "RequirementDefinition"
  }
}
```

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `syson.ai.enabled` | true | Enable/disable AI services |
| `syson.ai.provider` | gemini | AI provider (gemini/openai) |
| `syson.ai.timeout` | 60000 | API timeout (ms) |
| `syson.ai.gemini.api-key` | - | Gemini API key |
| `syson.ai.gemini.model` | gemini-2.0-flash-exp | Model name |
| `syson.ai.gemini.temperature` | 0.7 | Generation temperature (0-2) |
| `syson.ai.gemini.max-output-tokens` | 8192 | Max response tokens |

## Frontend Integration

### JavaScript/TypeScript Example

```typescript
// Chat with AI
const response = await fetch('/api/ai/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    message: 'How do I define a requirement?',
    context: {
      elementType: 'RequirementDefinition'
    }
  })
});

const aiResponse = await response.text();
console.log(aiResponse);

// Streaming code generation
const eventSource = new EventSource('/api/ai/code/generate/stream');
eventSource.addEventListener('code-chunk', (event) => {
  console.log('Code chunk:', event.data);
  // Append to editor
});
```

## Development

### Building

```bash
cd backend/services/syson-ai-services
mvn clean install
```

### Testing

```bash
mvn test
```

## Troubleshooting

### API Key Issues

- Ensure API key is set correctly
- Check logs for authentication errors
- Verify API key has proper permissions

### Timeout Errors

- Increase `syson.ai.timeout` for complex requests
- Check network connectivity to AI provider
- Consider using streaming endpoints for long operations

### Model Response Issues

- Adjust temperature for more/less creative responses
- Increase max tokens for longer responses
- Try different models (gemini-1.5-pro for higher quality)

## License

Eclipse Public License 2.0
