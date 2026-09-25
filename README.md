# Implemented
```table
| Check                                | Third-party API? | Can implement ourselves? |
| ------------------------------------ | ---------------: | -----------------------: |
| Email syntax                         |               No |                      Yes |
| Email length                         |               No |                      Yes |
| Local-part format                    |               No |                      Yes |
| Domain format                        |               No |                      Yes |
| Domain label validation              |               No |                      Yes |
| IDN/domain normalization             |               No |                      Yes |
| Role address detection               |               No |                      Yes |
| Disposable domain lookup from our DB |               No |                      Yes |
| Blacklisted email lookup             |               No |                      Yes |
| Blacklisted domain lookup            |               No |                      Yes |
| Local reputation database            |               No |                      Yes |
| Domain allow/deny lists              |               No |                      Yes |

```

# Remaining
DNS checks

MX check

SMTP verification

Checks that normally require an external provider/API

Email reputation

Domain age

Disposable email intelligence

Breached email detection

Spam intelligence


31. Recommended check master data

Initially I would create these checks:

1. EMAIL_FORMAT
2. DOMAIN_FORMAT
3. DOMAIN_DNS
4. MAIL_SERVER
5. BLACKLIST
6. ROLE_ADDRESS
7. SMTP

Then later:

8. SPF
9. DKIM
10. DMARC
11. DOMAIN_AGE
12. DISPOSABLE
13. REPUTATION
14. BREACH
15. SPAM_REPUTATION
16. FRAUD_REPUTATION

33. Final check architecture
34. 

```css

                         API Request
                              │
                              ▼
                         User ID
                              │
                              ▼
                    Active Subscription
                              │
                              ▼
                 UserSubscriptionCheck
                              │
                 ┌────────────┴────────────┐
                 │                         │
            allowed=true              allowed=false
                 │                         │
            enabled=true                SKIP
                 │
                 ▼
          Enabled User Checks
                 │
                 ▼
          Validator Registry
                 │
       ┌─────────┼──────────┐
       ▼         ▼          ▼
 EMAIL_FORMAT  DOMAIN_DNS  BLACKLIST
       │         │          │
       └─────────┼──────────┘
                 ▼
        ValidationDetail
                 │
                 ▼
       ValidationExecution
                 │
                 ▼
       totalPoints / earnedPoints
                 │
                 ▼
              score
                 │
                 ▼
          riskPercentage
                 │
                 ▼
           ScoreLevel
                 │
                 ▼
           EmailStatus
```