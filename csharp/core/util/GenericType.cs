using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core.util
{
    /**
     * Emulate Java's Class<?> generic type
     */
    public class GenericType<T>
    {

        public readonly Type Typee;

        public GenericType(T typee)
        {
            this.Typee = typee.GetType();
        }

        public GenericType(Type typee)
        {
            this.Typee = typee?.GetType();
        }


        //
        // Summary:
        //     Determines whether the specified object instances are considered equal.
        //
        // Parameters:
        //   objA:
        //     The first object to compare.
        //
        //   objB:
        //     The second object to compare.
        //
        // Returns:
        //     true if the objects are considered equal; otherwise, false. If both objA and
        //     objB are null, the method returns true.
        public bool Equals(Type obje)
        {
            return obje != null && obje == Typee;
        }

        //
        // Summary:
        //     Determines whether the specified object instances are considered equal.
        //
        // Parameters:
        //   objA:
        //     The first object to compare.
        //
        //   objB:
        //     The second object to compare.
        //
        // Returns:
        //     true if the objects are considered equal; otherwise, false. If both objA and
        //     objB are null, the method returns true.
        public bool Equals<F>(GenericType<F> obj) where F: T
        {
            return obj != null && obj.Typee == Typee;
        }
    }
}
