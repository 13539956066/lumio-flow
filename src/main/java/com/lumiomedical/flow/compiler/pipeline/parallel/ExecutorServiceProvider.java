package com.lumiomedical.flow.compiler.pipeline.parallel;

import java.util.concurrent.ExecutorService;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/06
 */
public interface ExecutorServiceProvider
{
    /**
     *
     * @return
     */
    ExecutorService provide();
}
