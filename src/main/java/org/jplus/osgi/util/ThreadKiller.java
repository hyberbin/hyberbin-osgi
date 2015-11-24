package org.jplus.osgi.util;


/// Kill a thread after a given timeout has elapsed
// <P>
// A simple timeout class.  You give it a thread to watch and a timeout
// in milliseconds.  After the timeout has elapsed, the thread is killed
// with a Thread.stop().  If the thread finishes successfully before then,
// you can cancel the timeout with a done() call; you can also re-use the
// timeout on the same thread with the reset() call.
// <P>
// <A HREF="/resources/classes/Acme/ThreadKiller.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>

public class ThreadKiller implements Runnable {

    private Thread targetThread;
    private Thread watcherThread;
    private boolean loop;
    private boolean enabled;

    /// Constructor.  Give it a thread to watch, and a timeout in milliseconds.
    // After the timeout has elapsed, the thread gets killed.  If you want
    // to cancel the kill, just call done().
    public ThreadKiller(Thread targetThread) {
        this.targetThread = targetThread;
        watcherThread = new Thread(this);
        enabled = true;
    }

    /// Call this when the target thread has finished.
    public synchronized void stop() {
        watcherThread.start();
    }


    /// The watcher thread - from the Runnable interface.
    // This has to be pretty anal to avoid monitor lockup, lost
    // threads, etc.
    public synchronized void run() {
        Thread me = Thread.currentThread();
        me.setPriority(Thread.MAX_PRIORITY);
        if (enabled) {
            do {
                loop = false;
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                }
            }
            while (enabled && loop);
        }
        if (enabled && targetThread.isAlive())
            targetThread.stop();
    }
}