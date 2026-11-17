using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;
using Quizzy.Domain.Enums;

namespace Quizzy.Infrastructure.Persistence;

/// <summary>
/// Populates the database with a demo account and illustrative content so the
/// application can be explored without entering data by hand. Intended for local
/// development only; callers must gate it on the development environment.
/// </summary>
public static class SampleDataSeeder
{
    public const string DemoEmail = "demo@quizzy.local";
    public const string DemoPassword = "demo12345";

    public static async Task SeedSampleDataAsync(
        QuizzyDbContext db,
        IPasswordHasher passwordHasher,
        CancellationToken cancellationToken = default)
    {
        if (await db.Users.AnyAsync(u => u.Email == DemoEmail, cancellationToken))
        {
            return;
        }

        var demo = new User
        {
            Email = DemoEmail,
            DisplayName = "Demo User",
            PasswordHash = passwordHasher.Hash(DemoPassword),
            Role = UserRole.User,
        };
        db.Users.Add(demo);

        var languages = new Folder { Name = "Languages", OwnerId = demo.Id };
        var computerScience = new Folder { Name = "Computer Science", OwnerId = demo.Id };
        db.Folders.AddRange(languages, computerScience);

        // A deck with more than one page of cards
        var spanish = new Deck
        {
            Title = "Spanish Vocabulary",
            Description = "Everyday words and simple phrases.",
            OwnerId = demo.Id,
            FolderId = languages.Id,
        };
        AddCards(spanish,
            ("Hola", "Hello"),
            ("Adiós", "Goodbye"),
            ("Gracias", "Thank you"),
            ("Por favor", "Please"),
            ("Sí", "Yes"),
            ("No", "No"),
            ("Agua", "Water"),
            ("Comida", "Food"),
            ("Casa", "House"),
            ("Perro", "Dog"),
            ("Gato", "Cat"),
            ("Libro", "Book"));

        var sql = new Deck
        {
            Title = "SQL Statements",
            Description = "What the core SQL keywords do.",
            OwnerId = demo.Id,
            FolderId = computerScience.Id,
        };
        AddCards(sql,
            ("SELECT", "Retrieves rows from one or more tables"),
            ("INSERT", "Adds new rows to a table"),
            ("UPDATE", "Modifies existing rows"),
            ("DELETE", "Removes rows from a table"),
            ("JOIN", "Combines rows from related tables"));

        var capitals = new Deck
        {
            Title = "World Capitals",
            Description = "Capital cities around the world.",
            OwnerId = demo.Id,
        };
        AddCards(capitals,
            ("France", "Paris"),
            ("Japan", "Tokyo"),
            ("Egypt", "Cairo"),
            ("Brazil", "Brasília"),
            ("Canada", "Ottawa"),
            ("Australia", "Canberra"));

        db.Decks.AddRange(spanish, sql, capitals);

        // A quiz with more than one page of questions
        var spanishQuiz = new Quiz
        {
            Title = "Spanish Basics",
            Description = "Check your grasp of everyday Spanish.",
            OwnerId = demo.Id,
            FolderId = languages.Id,
        };
        var sq1 = Single("How do you say \"hello\"?", "Hola", "Adiós", "Gracias", "Por favor");
        var sq2 = Single("What does \"gracias\" mean?", "Thank you", "Please", "Sorry", "Goodbye");
        var sq3 = TrueFalse("\"Buenos días\" means \"good night\".", false);
        var sq4 = Multi("Which of these are greetings?",
            correct: ["Hola", "Buenos días"],
            wrong: ["Gracias", "Adiós"]);
        var sq5 = Single("How do you say \"please\"?", "Por favor", "Gracias", "De nada", "Lo siento");
        var sq6 = TrueFalse("\"Uno\" is the number one.", true);
        AddQuestions(spanishQuiz, sq1, sq2, sq3, sq4, sq5, sq6);

        var sqlQuiz = new Quiz
        {
            Title = "SQL Fundamentals",
            Description = "The basics of querying a relational database.",
            OwnerId = demo.Id,
            FolderId = computerScience.Id,
        };
        AddQuestions(sqlQuiz,
            Single("Which statement retrieves data?", "SELECT", "INSERT", "UPDATE", "DELETE"),
            Multi("Which of these are aggregate functions?",
                correct: ["COUNT", "SUM"],
                wrong: ["MERGE", "JOIN"]),
            TrueFalse("A PRIMARY KEY column can contain NULL values.", false),
            Single("Which clause filters rows?", "WHERE", "ORDER BY", "GROUP BY", "LIMIT"));

        db.Quizzes.AddRange(spanishQuiz, sqlQuiz);

        // A past attempt on the Spanish quiz: four of six answered correctly.
        var attempt = new QuizAttempt
        {
            QuizId = spanishQuiz.Id,
            UserId = demo.Id,
            Score = 4,
            MaxScore = 6,
        };
        AddCorrectAnswer(attempt, sq1);
        AddCorrectAnswer(attempt, sq2);
        AddCorrectAnswer(attempt, sq3);
        AddCorrectAnswer(attempt, sq4);
        AddWrongAnswer(attempt, sq5);
        AddWrongAnswer(attempt, sq6);
        db.QuizAttempts.Add(attempt);

        // A few consecutive days of activity so the streak and dashboard are populated.
        var today = DateOnly.FromDateTime(DateTime.UtcNow);
        for (var daysAgo = 0; daysAgo < 4; daysAgo++)
        {
            db.StudyActivities.Add(new StudyActivity
            {
                UserId = demo.Id,
                Date = today.AddDays(-daysAgo),
            });
        }

        await db.SaveChangesAsync(cancellationToken);

        static void AddCards(Deck deck, params (string Front, string Back)[] cards)
        {
            foreach (var (front, back) in cards)
            {
                deck.Flashcards.Add(new Flashcard { Front = front, Back = back });
            }
        }

        static void AddQuestions(Quiz quiz, params Question[] questions)
        {
            foreach (var question in questions)
            {
                quiz.Questions.Add(question);
            }
        }

        static Question Single(string text, string correct, params string[] wrong)
        {
            var question = new Question { Text = text, Type = QuestionType.SingleChoice };
            question.Options.Add(new AnswerOption { Text = correct, IsCorrect = true });
            foreach (var option in wrong)
            {
                question.Options.Add(new AnswerOption { Text = option, IsCorrect = false });
            }

            return question;
        }

        static Question Multi(string text, string[] correct, string[] wrong)
        {
            var question = new Question { Text = text, Type = QuestionType.MultipleChoice };
            foreach (var option in correct)
            {
                question.Options.Add(new AnswerOption { Text = option, IsCorrect = true });
            }

            foreach (var option in wrong)
            {
                question.Options.Add(new AnswerOption { Text = option, IsCorrect = false });
            }

            return question;
        }

        static Question TrueFalse(string text, bool answer)
        {
            var question = new Question { Text = text, Type = QuestionType.TrueFalse };
            question.Options.Add(new AnswerOption { Text = "True", IsCorrect = answer });
            question.Options.Add(new AnswerOption { Text = "False", IsCorrect = !answer });
            return question;
        }

        static void AddCorrectAnswer(QuizAttempt attempt, Question question)
        {
            attempt.Answers.Add(new AttemptAnswer
            {
                QuestionId = question.Id,
                SelectedOptionIds =
                [
                    .. question.Options
                        .Where(option => option.IsCorrect)
                        .Select(option => option.Id),
                ],
                IsCorrect = true,
            });
        }

        static void AddWrongAnswer(QuizAttempt attempt, Question question)
        {
            var distractor = question.Options.First(option => !option.IsCorrect);
            attempt.Answers.Add(new AttemptAnswer
            {
                QuestionId = question.Id,
                SelectedOptionIds = [distractor.Id],
                IsCorrect = false,
            });
        }
    }
}
