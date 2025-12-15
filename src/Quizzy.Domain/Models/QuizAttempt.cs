using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Quizzy.Domain.Models
{
    public class QuizAttempt
    {
        public ulong Id { get; set; }

        public ulong QuizId { get; set; }
        public Quiz Quiz { get; set; }

        public DateTimeOffset StartTime { get; set; }
        public DateTimeOffset? EndTime { get; set; }
    }
}
