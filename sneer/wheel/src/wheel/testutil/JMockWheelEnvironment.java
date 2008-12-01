package wheel.testutil;

import java.lang.reflect.Method;

import org.jmock.integration.junit4.JMock;
import org.junit.internal.runners.InitializationError;
import org.junit.runner.notification.RunNotifier;

import wheel.lang.ByRef;
import wheel.lang.Environments;
import wheel.lang.Environment;
import wheel.lang.exceptions.WheelExceptionHandler;
import wheel.lang.exceptions.impl.WheelExceptionHandlerImpl;

public class JMockWheelEnvironment extends JMock {
	
	public JMockWheelEnvironment(Class<?> testClass) throws InitializationError {
		super(testClass);
	}
	
	@Override
	protected Object createTest() throws Exception {
		final ByRef<Object> result = ByRef.newInstance();
		Environments.runWith(environment(), new Runnable() { @Override public void run()  {
			try {
				result.value = superCreateTest();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}});
		return result.value;
	}
	
	private Object superCreateTest() throws Exception {
		return super.createTest();
	}
	
	@Override
	protected void invokeTestMethod(final Method arg0, final RunNotifier arg1) {
		Environments.runWith(environment(), new Runnable() { @Override public void run() {
			superInvokeTestMethod(arg0, arg1);
		}});
	}
	
	protected void superInvokeTestMethod(Method arg0, RunNotifier arg1) {
		super.invokeTestMethod(arg0, arg1);
	}
	
	private Environment environment() {
		return new Environment() {
			@Override
			public <T> T provide(Class<T> intrface) {
				if (WheelExceptionHandler.class == intrface)
					return (T) new WheelExceptionHandlerImpl();
				return null;
			}
		};
	}
}