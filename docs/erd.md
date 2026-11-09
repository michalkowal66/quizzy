# Data model (ERD)

The application stores its data in PostgreSQL. Every table has a GUID primary key
(`Id`) and a `CreatedAt` timestamp inherited from a shared base entity. The diagram
below shows the entities and their relationships.

```mermaid
erDiagram
    User ||--o{ Folder : owns
    User ||--o{ Deck : owns
    User ||--o{ Quiz : owns
    User ||--o{ QuizAttempt : takes
    User ||--o{ StudyActivity : logs
    Folder |o--o{ Deck : groups
    Folder |o--o{ Quiz : groups
    Deck ||--o{ Flashcard : contains
    Quiz ||--o{ Question : contains
    Question ||--o{ AnswerOption : offers
    Quiz ||--o{ QuizAttempt : "taken in"
    QuizAttempt ||--o{ AttemptAnswer : records

    User {
        guid Id PK
        string Email
        string DisplayName
        string PasswordHash
        UserRole Role
        datetime CreatedAt
    }
    Folder {
        guid Id PK
        string Name
        guid OwnerId FK
        datetime CreatedAt
    }
    Deck {
        guid Id PK
        string Title
        string Description
        guid OwnerId FK
        guid FolderId FK
        datetime CreatedAt
        datetime UpdatedAt
    }
    Flashcard {
        guid Id PK
        guid DeckId FK
        string Front
        string Back
        datetime CreatedAt
    }
    Quiz {
        guid Id PK
        string Title
        string Description
        guid OwnerId FK
        guid FolderId FK
        datetime CreatedAt
        datetime UpdatedAt
    }
    Question {
        guid Id PK
        guid QuizId FK
        string Text
        QuestionType Type
        datetime CreatedAt
    }
    AnswerOption {
        guid Id PK
        guid QuestionId FK
        string Text
        bool IsCorrect
        datetime CreatedAt
    }
    QuizAttempt {
        guid Id PK
        guid QuizId FK
        guid UserId FK
        int Score
        int MaxScore
        datetime CreatedAt
    }
    AttemptAnswer {
        guid Id PK
        guid QuizAttemptId FK
        guid QuestionId
        guid_list SelectedOptionIds
        bool IsCorrect
        datetime CreatedAt
    }
    StudyActivity {
        guid Id PK
        guid UserId FK
        date Date
        datetime CreatedAt
    }
```

## Notes

- `Deck.FolderId` and `Quiz.FolderId` are optional: a deck or quiz may sit directly under
  its owner without belonging to a folder.
- `AttemptAnswer.QuestionId` and `AttemptAnswer.SelectedOptionIds` are stored as plain
  values rather than enforced foreign keys. An attempt is an immutable record of what the
  user answered, so it stays intact even if the quiz is edited or a question is removed
  afterwards.
- `StudyActivity` has a unique index on `(UserId, Date)` so a user has at most one activity
  record per calendar day, which is what the streak calculation counts.
