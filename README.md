# ExamMate Backend
## Overview
This backend module handles quizzes for the ExamMate application. It allows quiz creation, question management, result calculation, and enforces time limits for each quiz.

## Features
Create a quiz with:
- Unique ID
- Title
- List of questions
- Time limit (minutes)
- Deadline (date & time)
- Store and retrieve quiz data
- Enforce time rules
- Calculate quiz results based on submitted answers

## Usages
### Get Quizzes
- Endpoint
```
http://localhost:8080/api/v1/quiz
```
- Response Body
```json
[
    {
        "id": "quiz1",
        "title": "General Knowledge Quiz",
        "timeLimit": "PT3M",
        "questions": [
            {
                "text": "What is the capital of France?",
                "options": [
                    "Berlin",
                    "Madrid",
                    "Paris",
                    "Rome"
                ]
            },
            {
                "text": "Which planet is known as the Red Planet?",
                "options": [
                    "Earth",
                    "Mars",
                    "Jupiter",
                    "Saturn"
                ]
            }
        ]
    }
]
```

### Get Quiz By Id
- Endpoint: Path variable = :quiz-id
```
http://localhost:8080/api/v1/quiz/:quiz-id
```
- Response
```json
{
    "id": "quiz1",
    "title": "General Knowledge Quiz",
    "timeLimit": "PT3M",
    "questions": [
        {
            "id": "q1",
            "text": "What is the capital of France?",
            "options": [
                "Berlin",
                "Madrid",
                "Paris",
                "Rome"
            ]
        },
        {
            "id": "q2",
            "text": "Which planet is known as the Red Planet?",
            "options": [
                "Earth",
                "Mars",
                "Jupiter",
                "Saturn"
            ]
        }
    ]
}
```

### Submit Quiz
- Endpoint: Path variable = :quiz-id
```
http://localhost:8080/api/v1/quiz/submit/:quiz-id
```
- Request Body
```json
{
    "userId": "userId",
    "answerSubmissions": [
        {
            "questionId": "q1",
            "selectedAnswer": "Rome"
        },
        {
            "questionId": "q2",
            "selectedAnswer": "Mars"
        }
    ]
}
```
- Response
```json
{
    "questionResultResponse": [
        {
            "text": "q1",
            "options": [
                "Berlin",
                "Madrid",
                "Paris",
                "Rome"
            ],
            "correctAnswer": "Paris",
            "correct": false
        },
        {
            "text": "q2",
            "options": [
                "Earth",
                "Mars",
                "Jupiter",
                "Saturn"
            ],
            "correctAnswer": "Mars",
            "correct": true
        }
    ],
    "totalQuestions": 10,
    "correctAnswers": 9,
    "scorePercentage": 90.0
}
```