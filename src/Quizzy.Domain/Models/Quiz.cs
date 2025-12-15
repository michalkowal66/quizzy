using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Quizzy.Domain.Models
{
    public class Quiz
    {
        public ulong Id { get; set; }
        public string Name { get; set; }
        public IEnumerable<Question> Questions { get; set; }
    }
}
