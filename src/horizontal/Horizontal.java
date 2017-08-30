/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horizontal;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a.patriarca
 */
public class Horizontal
{

    private long returnId;
    private final Map<Long, Object> value;
    private final Map<Long, Boolean> isReady;

    public Horizontal()
    {
        this.returnId = 0;
        value = new ConcurrentHashMap<>();
        isReady = new ConcurrentHashMap<>();
    }

    public void run(Object job, String methodName)
    {
        threadStart(job, methodName, false, 0);
    }
    
    public void run(Object job, String methodName, Object args)
    {
        threadStart(job, methodName, false, 0, args);
    }

    public long runWithReturn(Object job, String methodName)
    {
        long returnIndex = returnId;
        returnId++;
        isReady.put(returnIndex, false);
        threadStart(job, methodName, true, returnIndex);
        return returnIndex;
    }
    
    public long runWithReturn(Object job, String methodName, Object args)
    {
        long returnIndex = returnId;
        returnId++;
        isReady.put(returnIndex, false);
        threadStart(job, methodName, true, returnIndex, args);
        return returnIndex;
    }

    public void runWithThread(Object job, String methodName, int thread)
    {
        for (int i = 0; i < thread; i++)
            run(job, methodName);
    }
    
    public void runWithThread(Object job, String methodName, int thread, Object args)
    {
        for (int i = 0; i < thread; i++)
            run(job, methodName, args);
    }

    public Set<Long> runWithThreadAndReturn(Object job, String methodName, int thread)
    {
        Set<Long> returnIds = new HashSet<>();
        for (int i = 0; i < thread; i++)
            returnIds.add(runWithReturn(job, methodName));
        return returnIds;
    }
    
    public Set<Long> runWithThreadAndReturn(Object job, String methodName, int thread, Object args)
    {
        Set<Long> returnIds = new HashSet<>();
        for (int i = 0; i < thread; i++)
            returnIds.add(runWithReturn(job, methodName, args));
        return returnIds;
    }

    public void runOnMaxCores(Object job, String methodName)
    {
        runWithThread(job, methodName, Runtime.getRuntime().availableProcessors());
    }
    
    public void runOnMaxCores(Object job, String methodName, Object args)
    {
        runWithThread(job, methodName, Runtime.getRuntime().availableProcessors(), args);
    }

    public Set<Long> runOnMaxCoresWithReturn(Object job, String methodName)
    {
        return runWithThreadAndReturn(job, methodName, Runtime.getRuntime().availableProcessors());
    }
    
    public Set<Long> runOnMaxCoresWithReturn(Object job, String methodName, Object args)
    {
        return runWithThreadAndReturn(job, methodName, Runtime.getRuntime().availableProcessors(), args);
    }

    public Object getValue(long index)
    {
        return value.get(index);
    }

    public boolean isReady(long index)
    {
        return isReady.get(index);
    }

    private void threadStart(Object job, String methodName, boolean returnMode, long returnIndex)
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if (returnMode)
                    {
                        value.put(returnIndex, job.getClass().getDeclaredMethod(methodName).invoke(job));
                        isReady.put(returnIndex, true);
                    } else
                        job.getClass().getDeclaredMethod(methodName).invoke(job);
                }
                catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e)
                {
                    Logger.getLogger(Horizontal.class.getName()).log(Level.SEVERE, null, e); 
                }

            }
        };
        thread.start();
    }
    
    private void threadStart(Object job, String methodName, boolean returnMode, long returnIndex, Object args)
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if (returnMode)
                    {
                        value.put(returnIndex, job.getClass().getDeclaredMethod(methodName, args.getClass()).invoke(job, args));
                        isReady.put(returnIndex, true);
                    } else
                        job.getClass().getDeclaredMethod(methodName, args.getClass()).invoke(job, args);
                }
                catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e)
                {
                    Logger.getLogger(Horizontal.class.getName()).log(Level.SEVERE, null, e);
                }

            }
        };
        thread.start();
    }
}
