package com.offbynull.coroutines.instrumenter;

import static com.offbynull.coroutines.instrumenter.TestUtils.loadClassesInZipResourceAndInstrument;
import com.offbynull.coroutines.user.Coroutine;
import com.offbynull.coroutines.user.CoroutineRunner;
import java.net.URLClassLoader;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class InstrumenterTest {

    private static final String NORMAL_INVOKE_TEST = "NormalInvokeTest";
    private static final String STATIC_INVOKE_TEST = "StaticInvokeTest";
    private static final String INTERFACE_INVOKE_TEST = "InterfaceInvokeTest";
    private static final String CONSTRUCTOR_INVOKE_TEST = "ConstructorInvokeTest";
    private static final String EXCEPTION_SUSPEND_TEST = "ExceptionSuspendTest";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void mustProperlySuspendWithVirtualMethods() throws Exception {
        performCountTest(NORMAL_INVOKE_TEST);
    }
    
    @Test
    public void mustProperlySuspendWithStaticMethods() throws Exception {
        performCountTest(STATIC_INVOKE_TEST);
    }

    @Test
    public void mustProperlySuspendWithInterfaceMethods() throws Exception {
        performCountTest(INTERFACE_INVOKE_TEST);
    }

    private void performCountTest(String testClass) throws Exception {
        StringBuilder builder = new StringBuilder();

        try (URLClassLoader classLoader = loadClassesInZipResourceAndInstrument(testClass + ".zip")) {
            Class<Coroutine> cls = (Class<Coroutine>) classLoader.loadClass(testClass);
            Coroutine coroutine = ConstructorUtils.invokeConstructor(cls, builder);

            CoroutineRunner runner = new CoroutineRunner(coroutine);

            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertFalse(runner.execute()); // coroutine finished executing here
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());

            Assert.assertEquals("started\n"
                    + "0\n"
                    + "1\n"
                    + "2\n"
                    + "3\n"
                    + "4\n"
                    + "5\n"
                    + "6\n"
                    + "7\n"
                    + "8\n"
                    + "9\n"
                    + "started\n"
                    + "0\n"
                    + "1\n"
                    + "2\n", builder.toString());
        }
    }

    @Test
    public void mustNotInstrumentConstructors() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Instrumentation of constructors not allowed");

        try (URLClassLoader classLoader = loadClassesInZipResourceAndInstrument(CONSTRUCTOR_INVOKE_TEST + ".zip")) {
            // do nothing, exception will occur
        }
    }

    @Test
    public void mustProperlySuspendInTryCatchFinally() throws Exception {
        StringBuilder builder = new StringBuilder();

        try (URLClassLoader classLoader = loadClassesInZipResourceAndInstrument(EXCEPTION_SUSPEND_TEST + ".zip")) {
            Class<Coroutine> cls = (Class<Coroutine>) classLoader.loadClass(EXCEPTION_SUSPEND_TEST);
            Coroutine coroutine = ConstructorUtils.invokeConstructor(cls, builder);

            CoroutineRunner runner = new CoroutineRunner(coroutine);

            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertTrue(runner.execute());
            Assert.assertFalse(runner.execute()); // coroutine finished executing here

            Assert.assertEquals(
                    "START\n"
                    + "IN TRY 1\n"
                    + "IN TRY 2\n"
                    + "IN CATCH 1\n"
                    + "IN CATCH 2\n"
                    + "IN FINALLY 1\n"
                    + "IN FINALLY 2\n"
                    + "END\n", builder.toString());
        }
    }
}
