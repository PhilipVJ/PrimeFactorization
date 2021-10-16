using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace PP
{
    class Program
    {
        public static Tuple<long, long> result;
        static void Main(string[] args)
        {

            Factorization();
        }

        public static void Factorization()
        {
            var product = 12337069;            
            var primes = GetPrimes(product);

            Stopwatch watch = new Stopwatch();
            watch.Start();            

            var rangePartitioner = Partitioner.Create(0, primes.Count);
            Parallel.ForEach(rangePartitioner, (range, loopState) =>
            { 
                // Job partition
                for (int i = range.Item1; i < range.Item2; i++)
                {                    
                    int stepStart = i + 1;
                    long multiplyValue = primes[stepStart - 1];
                    for (int j = stepStart; j < primes.Count; j++)
                    {
                        if (loopState.IsStopped)
                        {         
                            return;
                        }
                        long value = primes[j];
                        if (multiplyValue * value == product)
                        {
                            Console.WriteLine("Found!");
                            result = Tuple.Create(value, multiplyValue);
                            loopState.Stop();
                        }                                   
                    }

                }
            });
            if (result != null)
            {
                Console.WriteLine("Found result: ");
                Console.WriteLine(result);
            } else
            {
                Console.WriteLine("Did not find a result");
            }

            watch.Stop();
            Console.WriteLine("Time: " + (watch.ElapsedMilliseconds / 1000));
        }

        public static List<int> GetPrimes(int product)
        {
            Stopwatch watch = new Stopwatch();
            watch.Start();
            var range = Enumerable.Range(1, product);
            var primes = from num in range.AsParallel()
                         where Check_Prime(num)
                         select num;

            Console.WriteLine("Length: " + primes.Count());
            watch.Stop();
            Console.WriteLine("Time: " + (watch.ElapsedMilliseconds / 1000));
            return primes.ToList();
        }              

        private static bool Check_Prime(long number)
        {            
            if (number < 2) return false;
            if (number % 2 == 0) return (number == 2);
            int root = (int)Math.Sqrt((double)number);
            for (int i = 3; i <= root; i += 2)
            {
                if (number % i == 0) return false;
            }
            return true;
        }
    }
}
