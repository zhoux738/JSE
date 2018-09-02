package info.jultest.test;

public class TestSession implements AutoCloseable {

	private Thread t;
	private String group;
	
	public TestSession(Thread t, String group) {
		this.t = t;
		this.group = group;
	}

	public int getIntUntil(String key, int min, int max){
		while(true) {
			int res = TestContext.getInt(group, key);
			if (res < min || res > max) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					
				}
			} else {
				return res;
			}
		}
	}

    public void assertBool(String key, boolean value) {
        while(true) {
            Boolean res = TestContext.getBool(group, key);
            if (res != null && res.booleanValue() == value) {
                return;
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    
                }
            }
        }
    }
	
	public void waitForCompletion(){
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void close() throws Exception {
		waitForCompletion();
	}
}
