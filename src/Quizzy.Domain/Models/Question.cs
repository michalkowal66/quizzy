using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Quizzy.Domain.Models
{
    public class Question
    {
        public ulong Id { get; set; }
        public string Text { get; set; }
        public IEnumerable<Answer> Answers { get; set; }
    }
}
