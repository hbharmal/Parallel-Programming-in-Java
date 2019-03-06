class BlockingQueue {

	final int size = 10;
	double[] buffer = new double[size];
	BinarySemaphore mutex = new BinarySemaphore(true);
	CountingSemaphore isEmpty = new CountingSemaphore(0);
	CountingSemaphore isFull = new CountingSemaphore(size);
	int inBuf = 0;
	int outBuf = 0;

	// Deposit item in Queue (@ location inBuf)
	public void deposit(double value) {
		isFull.P();
		mutex.P();
		buffer[inBuf] = value;
		inBuf = (inBuf + 1) % size;
		mutex.V();
		isFull.V();
	}

	// Fetch item from Queue (@ location outBuf)
	public void fetch() {
		isEmpty.P();
		mutex.P();
		double value = buffer[outBuf];
		outBuf = (outBuf + 1) % size;
		mutex.V();
		isFull.V();
		return value;
	}

}