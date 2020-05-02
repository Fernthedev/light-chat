using System;

namespace com.github.fernthedev.lightchat.core.encryption
{
    [Serializable]
    public class HashedPassword
    {
        public HashedPassword(string password)
        {
            this.password = password;
        }

        public string password { get; }


    }
}