# Challenge Validation Endpoint

## Overview
A new `/api/v1/challenge/validate` endpoint that validates a previously created challenge against:
1. SHA256 hash existence in the database
2. Creation timestamp accuracy
3. TTL expiration status

## Endpoint Details

### Request
**Endpoint:** `POST /api/v1/challenge/validate`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
  "createdAt": "2026-03-28T10:30:00Z"
}
```

**Field Requirements:**
- `sha` (required): Must be exactly 64 hexadecimal characters (SHA256 format)
- `createdAt` (required): Must be a valid ISO 8601 Instant timestamp

### Response (200 OK)

**Success Case:**
```json
{
  "isValid": true,
  "message": "Challenge is valid"
}
```

**Failure Cases:**
```json
{
  "isValid": false,
  "message": "SHA is required and cannot be empty"
}
```

```json
{
  "isValid": false,
  "message": "SHA must be exactly 64 characters long"
}
```

```json
{
  "isValid": false,
  "message": "SHA must contain only hexadecimal characters (0-9, a-f)"
}
```

```json
{
  "isValid": false,
  "message": "Challenge not found"
}
```

```json
{
  "isValid": false,
  "message": "Challenge creation timestamp mismatch"
}
```

```json
{
  "isValid": false,
  "message": "Challenge has expired"
}
```

## Validation Logic

### Step 1: Input Validation (Before DB Access)
Both fields are validated **before** querying the database:
- SHA cannot be empty
- SHA must be exactly 64 characters
- SHA must contain only hexadecimal characters (0-9, a-f)
- createdAt must be a valid Instant (JSON deserialization validates this)

If any input validation fails, the API returns immediately without touching the database.

### Step 2: Database Lookup
- Look up the challenge by SHA in the database
- If not found, return "Challenge not found"

### Step 3: Timestamp Verification
- Verify the `createdAt` in the request matches the database record
- This prevents tampering with the creation time
- If mismatch, return "Challenge creation timestamp mismatch"

### Step 4: TTL Expiration Check
- Get the challenge's `expiresAt` from the database
- Compare current time with `expiresAt`
- If current time > expiresAt, return "Challenge has expired"
- Otherwise, validation succeeds

## Error Handling

### Bad Request (400)
Returned if the JSON request body is malformed or missing required fields:
```bash
# Missing "sha" field
POST /api/v1/challenge/validate
{"createdAt": "2026-03-28T10:30:00Z"}

# Response: 400 Bad Request
```

### OK (200)
**Always** returned for well-formed requests (regardless of validation result).
The `isValid` boolean in the response indicates success/failure.

```bash
# Challenge expired but request is valid
POST /api/v1/challenge/validate
{"sha": "a...f2", "createdAt": "2026-03-20T10:30:00Z"}

# Response: 200 OK
{
  "isValid": false,
  "message": "Challenge has expired"
}
```

## Examples

### Example 1: Valid Challenge
```bash
curl -X POST http://localhost:8081/api/v1/challenge/validate \
  -H "Content-Type: application/json" \
  -d '{
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
    "createdAt": "2026-03-28T10:30:00Z"
  }'

# Response: 200 OK
{
  "isValid": true,
  "message": "Challenge is valid"
}
```

### Example 2: Expired Challenge
```bash
curl -X POST http://localhost:8081/api/v1/challenge/validate \
  -H "Content-Type: application/json" \
  -d '{
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
    "createdAt": "2026-03-20T10:30:00Z"
  }'

# Response: 200 OK
{
  "isValid": false,
  "message": "Challenge has expired"
}
```

### Example 3: Invalid SHA Format
```bash
curl -X POST http://localhost:8081/api/v1/challenge/validate \
  -H "Content-Type: application/json" \
  -d '{
    "sha": "invalid_sha",
    "createdAt": "2026-03-28T10:30:00Z"
  }'

# Response: 200 OK
{
  "isValid": false,
  "message": "SHA must be exactly 64 characters long"
}
```

## Files Modified/Created

**New DTOs:**
- `src/main/kotlin/com/openpad/dto/ChallengeValidationRequestDto.kt`
- `src/main/kotlin/com/openpad/dto/ChallengeValidationResponseDto.kt`

**Modified Service:**
- `src/main/kotlin/com/openpad/service/ChallengeService.kt`
  - Added `validateChallenge()` method
  - Added `validateShaFormat()` helper method

**Modified Controller:**
- `src/main/kotlin/com/openpad/controller/ChallengeController.kt`
  - Added `validateChallenge()` endpoint

**New Tests:**
- Added 7 new service tests for validation logic
- Added 6 new controller tests for endpoint behavior

## Test Coverage

### Service Tests (7 new tests)
1. Empty SHA validation
2. SHA length validation
3. SHA format validation (hex chars only)
4. Challenge not found in database
5. Creation timestamp mismatch detection
6. Expired challenge detection
7. Valid challenge acceptance

### Controller Tests (6 new tests)
1. Valid challenge response (200 OK, isValid=true)
2. Invalid challenge response (200 OK, isValid=false)
3. Missing SHA rejection (400 Bad Request)
4. Missing createdAt rejection (400 Bad Request)
5. Challenge not found response
6. Timestamp mismatch response

## Architecture

### Clean Separation of Concerns
```
Controller (HTTP plumbing)
    ↓
Service (validation logic + DB access)
    ├─ Input validation (before DB)
    ├─ Database lookup
    ├─ Timestamp verification
    └─ TTL expiration check
    ↓
Repository (data access)
    ↓
Database
```

### DTO Pattern
- Request DTO: `ChallengeValidationRequestDto` (decouples API from internal models)
- Response DTO: `ChallengeValidationResponseDto` (consistent API contract)

## Security Notes

1. **Input Validation First**: All input is validated before database access (prevents injection, reduces DB load)
2. **Timestamp Verification**: Client must provide exact creation timestamp (prevents SHA reuse with different start times)
3. **TTL Enforcement**: Expired challenges are rejected (prevents replay attacks)
4. **Database Lookup**: SHA-based lookup ensures each challenge is unique and traceable

## Running Tests

```bash
# Run all tests
./gradlew test

# Run only validation service tests
./gradlew test --tests ChallengeServiceTest

# Run only validation controller tests
./gradlew test --tests ChallengeControllerTest

# Run with detailed output
./gradlew test --info
```

## Summary

✅ Validation endpoint created at `/api/v1/challenge/validate`
✅ Input validation before database access
✅ Comprehensive error messages
✅ TTL expiration checking
✅ Timestamp verification
✅ 13 new test cases covering all validation paths
✅ Clean architecture with separation of concerns
