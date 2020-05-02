using System.Collections.Generic;
using System.Linq;

namespace com.github.fernthedev.lightchat.core.util
{
    public class ConvertUtil
    {
        public static List<byte> fromIntList(IEnumerable<int> list)
        {
            return list.Select(i => (byte) i).ToList();
        }

        public static List<int> toIntList(IEnumerable<byte> list)
        {
            return list.Select(i => (int) i).ToList();
        }
    }
}