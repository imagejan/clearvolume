package clearvolume.network.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import clearvolume.volume.Volume;
import clearvolume.volume.sink.VolumeSinkInterface;
import clearvolume.volume.source.SourceToSinkBufferedAdapter;

public class ClearVolumeTCPServer	implements
																	Closeable,
																	VolumeSinkInterface
{

	private ServerSocketChannel mServerSocketChannel;

	private ClearVolumeTCPServerRunnable lRunnable;
	private Thread mRunnableThread;

	private SourceToSinkBufferedAdapter mSourceToSinkBufferedAdapter;

	public ClearVolumeTCPServer(int pMaxCapacity)
	{
		super();
		mSourceToSinkBufferedAdapter = new SourceToSinkBufferedAdapter(pMaxCapacity);
	}

	public boolean open(SocketAddress pSocketAddress) throws IOException
	{
		if (mServerSocketChannel != null && mServerSocketChannel.isOpen())
			return false;
		mServerSocketChannel = ServerSocketChannel.open();
		mServerSocketChannel.socket().bind(pSocketAddress);
		mServerSocketChannel.configureBlocking(true);

		return mServerSocketChannel.isOpen();
	}

	@Override
	public void close() throws IOException
	{
		if (mServerSocketChannel == null || !mServerSocketChannel.isOpen())
		{
			mServerSocketChannel = null;
		}

		mServerSocketChannel.close();
		mServerSocketChannel = null;
	}

	public boolean start()
	{
		lRunnable = new ClearVolumeTCPServerRunnable(	mServerSocketChannel,
																									mSourceToSinkBufferedAdapter);
		mRunnableThread = new Thread(	lRunnable,
																	"ClearVolumeClientRunnableThread");
		mRunnableThread.setDaemon(true);
		mRunnableThread.start();
		return true;
	}

	public boolean stop()
	{
		lRunnable.requestStop();
		lRunnable.waitForStop();
		return true;
	}

	@Override
	public void sendVolume(Volume pVolume)
	{
		mSourceToSinkBufferedAdapter.sendVolume(pVolume);
	}

}
