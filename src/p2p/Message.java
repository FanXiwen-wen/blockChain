package p2p;

import java.io.Serializable;

/**
 * p2pͨѶ��Ϣ
 * �̳�SerializableʹMessage���ܹ����л�
 * @author Administrator
 *
 */
public class Message implements Serializable{
	//��Ϣ����
	private int type;
	//��Ϣ����
	private String data;
	
	public Message() {
		
	}
	public Message(int type) {
		this.type = type;
	}

	public Message(int type, String data) {
		this.type = type;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
