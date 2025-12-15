using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Quizzy.Domain.Models
{
    public class QuestionAnswer
    {
        public ulong AttemptId { get; set; }
        public QuizAttempt Attempt { get; set; }

        public ulong QuestionId { get; set; }
        public Question Question { get; set; }

        public ulong AnswerId { get; set; }
        public Answer Answer { get; set; }
    }
}
