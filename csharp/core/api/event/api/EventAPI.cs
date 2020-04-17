namespace com.github.fernthedev.lightchat.core.events
{
    public abstract class IEvent
    {
        private string name;
        private readonly bool async;

        /**
         * The default constructor is defined for cleaner code. This constructor
         * assumes the event is synchronous.
         */
        public IEvent() : this(false)
        {
        }

        /**
         * This constructor is used to explicitly declare an event as synchronous
         * or asynchronous.
         *
         * @param isAsync true indicates the event will fire asynchronously, false
         *     by default from default constructor
         */
        public IEvent(bool isAsync)
        {
            async = isAsync;
        }

        /**
         * Convenience method for providing a user-friendly identifier. By
         * default, it is the event's class's {@linkplain Class#getSimpleName()
         * simple name}.
         *
         * @return name of this event
         */
        public string getEventName()
        {
            if (name == null)
            {
                name = GetType().Name;
            }
            return name;
        }

        /**
        * Any custom event that should not by synchronized with other events must
        * use the specific constructor. These are the caveats of using an
        * asynchronous event:
        * <ul>
        * <li>The event is never fired from inside code triggered by a
        *     synchronous event. Attempting to do so results in an {@link
        *     IllegalStateException}.
        * <li>However, asynchronous event handlers may fire synchronous or
        *     asynchronous events
        * <li>The event may be fired multiple times simultaneously and in any
        *     order.
        * <li>Any newly registered or unregistered handler is ignored after an
        *     event starts execution.
        * <li>The handlers for this event may block for any length of time.
        * <li>Some implementations may selectively declare a specific event use
        *     as asynchronous. This behavior should be clearly defined.
        * <li>Asynchronous calls are not calculated in the plugin timing system.
        * </ul>
        *
        * @return false by default, true if the event fires asynchronously
        */
        public bool IsAsynchronous()
        {
            return async;
        }
    }

    public interface ICancellable
    {
        /**
         * Gets the cancellation state of this event. A cancelled event will not
         * be executed in the server, but will still pass to other plugins
         *
         * @return true if this event is cancelled
         */
        public bool isCancelled();

        /**
         * Sets the cancellation state of this event. A cancelled event will not
         * be executed in the server, but will still pass to other plugins.
         *
         * @param cancel true if you wish to cancel this event
         */
        public void setCancelled(bool cancel);
    }
}