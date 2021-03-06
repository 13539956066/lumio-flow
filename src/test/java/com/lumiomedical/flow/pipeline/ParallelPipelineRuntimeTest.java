package com.lumiomedical.flow.pipeline;

import com.lumiomedical.flow.Flow;
import com.lumiomedical.flow.FlowAssertion;
import com.lumiomedical.flow.FlowDealer;
import com.lumiomedical.flow.FlowState;
import com.lumiomedical.flow.compiler.CompilationException;
import com.lumiomedical.flow.compiler.RunException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/10
 */
public class ParallelPipelineRuntimeTest
{
    @Test
    void testSimpleStringEdit() throws RunException, CompilationException
    {
        var pipeAssertion = new FlowAssertion();

        var pipe = Flow.from(() -> "This is my string.")
            .into((s) -> {
                return s.replace(" is ", " is not ");
            })
            .into((value) -> {
                Assertions.assertEquals("This is not my string.", value);
                pipeAssertion.setActivated(true);
            });

        Flow.runAsParallelPipeline(pipe);
        Assertions.assertTrue(pipeAssertion.isActivated());
    }

    @Test
    void testSimpleArithmetic1() throws RunException, CompilationException
    {
        var pipeAssertion = new FlowAssertion();

        var pipe = FlowDealer.sourceReturns8().into((value) -> {
            Assertions.assertEquals(8, value);
            pipeAssertion.setActivated(true);
        });

        Flow.runAsParallelPipeline(pipe);
        Assertions.assertTrue(pipeAssertion.isActivated());
    }

    @Test
    void testSimpleArithmetic2() throws RunException, CompilationException
    {
        var pipeAssertion = new FlowAssertion();

        var pipe = FlowDealer.sourceReturns9().into((value) -> {
            Assertions.assertEquals(9, value);
            pipeAssertion.setActivated(true);
        });

        Flow.runAsParallelPipeline(pipe);
        Assertions.assertTrue(pipeAssertion.isActivated());
    }

    @Test
    void testSimpleArithmetic3() throws RunException, CompilationException
    {
        var pipeAssertion = new FlowAssertion();

        var pipe = FlowDealer.sourceReturns6().into((value) -> {
            Assertions.assertEquals(6, value);
            pipeAssertion.setActivated(true);
        });

        Flow.runAsParallelPipeline(pipe);
        Assertions.assertTrue(pipeAssertion.isActivated());
    }

    @Test
    void testSimpleArithmetic4() throws RunException, CompilationException
    {
        var pipeAssertion1 = new FlowAssertion();
        var pipeAssertion2 = new FlowAssertion();

        var pipe = FlowDealer.sourceReturns6();

        var sink1 = pipe.into((value) -> {
            Assertions.assertEquals(6, value);
            pipeAssertion1.setActivated(true);
        });
        var sink2 = pipe
            .into((value) -> value * 3)
            .into((value) -> {
                Assertions.assertEquals(18, value);
                pipeAssertion2.setActivated(true);
            });

        Flow.runAsParallelPipeline(sink1, sink2);
        Assertions.assertTrue(pipeAssertion1.isActivated());
        Assertions.assertTrue(pipeAssertion2.isActivated());
    }

    @Test
    void testSimpleArithmetic5() throws RunException, CompilationException
    {
        var pipeAssertion1 = new FlowAssertion();
        var pipeAssertion2 = new FlowAssertion();

        var pipe = FlowDealer.sourceReturns6();

        var sink1 = pipe.into((value) -> {
            Assertions.assertEquals(6, value);
            Assertions.assertTrue(pipeAssertion1.isActivated());
            pipeAssertion2.setActivated(true);
        });
        var sink2 = pipe
            .into((value) -> value * 3)
            .into((value) -> {
                Assertions.assertEquals(18, value);
                pipeAssertion1.setActivated(true);
            });

        sink1.after(sink2);

        Flow.runAsParallelPipeline(sink1, sink2);
        Assertions.assertTrue(pipeAssertion2.isActivated());
    }

    @Test
    void testSimpleJoin1() throws RunException, CompilationException
    {
        var pipeAssertion = new FlowAssertion();

        var pipe = FlowDealer.joinSub(
            FlowDealer.sourceReturns8(),
            FlowDealer.sourceReturns9()
        ).into((value) -> {
            Assertions.assertEquals(-1, value);
            pipeAssertion.setActivated(true);
        });

        Flow.runAsParallelPipeline(pipe);
        Assertions.assertTrue(pipeAssertion.isActivated());
    }

    @Test
    void testSimpleJoin2() throws RunException, CompilationException
    {
        var pipeAssertion = new FlowAssertion();

        var pipe = FlowDealer.joinSub(
            FlowDealer.joinMult(
                FlowDealer.sourceReturns8(),
                FlowDealer.sourceReturns9()
            ),
            FlowDealer.sourceReturns6()
        ).into((value) -> {
            Assertions.assertEquals(66, value);
            pipeAssertion.setActivated(true);
        });

        Flow.runAsParallelPipeline(pipe);
        Assertions.assertTrue(pipeAssertion.isActivated());
    }

    @Test
    void testDrift1() throws RunException, CompilationException
    {
        FlowState<Integer> stateA = new FlowState<>();
        FlowState<Integer> stateB = new FlowState<>();
        FlowState<Integer> stateC = new FlowState<>();

        var flow = Flow.from(() -> 10)
            .into(i -> i * 2)
            .drift(stateA::setValue)
            .into(i -> i / 4)
            .drift(stateB::setValue)
            .into(i -> i * 2)
            .into(stateC::setValue)
        ;

        Flow.runAsParallelPipeline(flow);

        Assertions.assertEquals(20, stateA.getValue());
        Assertions.assertEquals(5, stateB.getValue());
        Assertions.assertEquals(10, stateC.getValue());
    }

    @Test
    void testCollect1() throws RunException, CompilationException
    {
        var recipient = FlowDealer.joinSub(
            FlowDealer.joinMult(
                FlowDealer.sourceReturns8(),
                FlowDealer.sourceReturns9()
            ),
            FlowDealer.sourceReturns6()
        ).collect();

        Flow.runAsParallelPipeline(recipient);

        Assertions.assertEquals(66, recipient.getContent());
    }

    @Test
    void testCollect2() throws RunException, CompilationException
    {
        var recipient = FlowDealer.sourceReturns8()
            .into(i -> i * 3)
            .join(FlowDealer.sourceReturns9(), (a, b) -> a - b)
            .join(FlowDealer.sourceReturns6(), (a, b) -> a ^ b)
            .join(FlowDealer.sourceReturns8(), (a, b) -> ((float)a / (float)b))
            .collect()
        ;

        Flow.runAsParallelPipeline(recipient);

        Assertions.assertEquals(1.125F, recipient.getContent());
    }

    @Test
    void testSample1() throws RunException, CompilationException
    {
        var flow = FlowDealer.sourceReturns8()
            .into(i -> i * 3)
            .join(FlowDealer.sourceReturns9(), (a, b) -> a - b)
            .sample("a")
            .join(FlowDealer.sourceReturns6(), (a, b) -> a ^ b)
            .sample("b")
            .join(FlowDealer.sourceReturns8(), (a, b) -> ((float)a / (float)b))
        ;

        var runtime = Flow.runAsParallelPipeline(flow);

        Assertions.assertEquals(15, runtime.getSample("a", Integer.class));
        Assertions.assertEquals(9, runtime.getSample("b", Integer.class));
    }

    @Test
    void testSampleError1() throws RunException, CompilationException
    {
        var flow = FlowDealer.sourceReturns8()
            .into(i -> i * 3)
            .join(FlowDealer.sourceReturns9(), (a, b) -> a - b)
            .sample("a")
            .join(FlowDealer.sourceReturns6(), (a, b) -> a ^ b)
        ;

        var runtime = Flow.runAsParallelPipeline(flow);

        Assertions.assertThrows(RunException.class, () -> runtime.getSample("a", Double.class));
    }
}
