//class RTPpacket

public class RTPpacket{

  //size of the RTP header:
  static int HEADER_SIZE = 12;

  //Fields that compose the RTP header
  public int Version;
  public int Padding;
  public int Extension;
  public int CC;
  public int Marker;
  public int PayloadType;
  public int SequenceNumber;
  public int TimeStamp;
  public int Ssrc;
  
  //Bitstream of the RTP header
  public byte[] header;

  //size of the RTP payload
  public int payload_size;
  //Bitstream of the RTP payload
  public byte[] payload;
  


  //--------------------------
  //Constructor of an RTPpacket object from header fields and payload bitstream
  //--------------------------
  public RTPpacket(int PType, int Framenb, int Time, byte[] data, int data_length){
    //fill by default header fields:
    Version = 2;
    Padding = 0;
    Extension = 0;
    CC = 0;
    Marker = 0;
    Ssrc = 0;

    //fill changing header fields:
    SequenceNumber = Framenb;
    TimeStamp = Time;
    PayloadType = PType;
    
    //build the header bistream:
    //--------------------------
    header = new byte[HEADER_SIZE];

    /*

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |V=2|P|X|  CC   |M|     PT      |       sequence number         |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                           timestamp                           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |           synchronization source (SSRC) identifier            |
   +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
   |            contributing source (CSRC) identifiers             |
   |                             ....                              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

    To set bits n and n + 1 to the value of foo in variable mybyte:
     mybyte = mybyte | foo << (7 - n);

    */
    int firstbyte=0, secondbyte=0, thirdbyte=0, fourthbyte=0, 
        fifthbyte=0, sixthbyte=0, seventhbyte=0, eighthbyte=0, 
        ninth=0, tenth=0, eleventh=0, twelfth=0;

    firstbyte = firstbyte   | (Version    << (6));
    firstbyte = firstbyte   | (Padding    << (5));
    firstbyte = firstbyte   | (Extension  << (4));
    firstbyte = firstbyte   | (CC         << (0));

    secondbyte = secondbyte | (Marker       << (7));
    secondbyte = secondbyte | (PayloadType  << (0));

    /*
    To copy a 16-bit integer foo into 2 bytes, b1 and b2:
       b1 = foo >> 8;
       b2 = foo & 0xFF;
    */   
    thirdbyte  =  thirdbyte  | (SequenceNumber >> 8);
    fourthbyte =  fourthbyte | (SequenceNumber & 0xFF);

    fifthbyte   = fifthbyte   | (TimeStamp >> 24);
    sixthbyte   = sixthbyte   | (TimeStamp >> 16);
    seventhbyte = seventhbyte | (TimeStamp >> 8);
    eighthbyte  = eighthbyte  | (TimeStamp  & 0xFF);

    ninth    = ninth    | (Ssrc >> 24);
    tenth    = tenth    | (Ssrc >> 16);
    eleventh = eleventh | (Ssrc >> 8);
    twelfth  = twelfth  | (Ssrc & 0xFF);

    //fill the header array of byte with RTP header fields
    header[0]  = new Integer(firstbyte).byteValue();
    header[1]  = new Integer(secondbyte).byteValue();
    header[2]  = new Integer(thirdbyte).byteValue();
    header[3]  = new Integer(fourthbyte).byteValue();
    header[4]  = new Integer(fifthbyte).byteValue();
    header[5]  = new Integer(sixthbyte).byteValue();
    header[6]  = new Integer(seventhbyte).byteValue();
    header[7]  = new Integer(eighthbyte).byteValue();
    header[8]  = new Integer(ninth).byteValue();
    header[9]  = new Integer(tenth).byteValue();
    header[10] = new Integer(eleventh).byteValue();
    header[11] = new Integer(twelfth).byteValue();

    //fill the payload bitstream:
    //--------------------------
    payload_size = data_length;
    payload = new byte[data_length];

    //fill payload array of byte from data (given in parameter of the constructor)
    payload = data;

  }
    
  //--------------------------
  //Constructor of an RTPpacket object from the packet bistream 
  //--------------------------
  public RTPpacket(byte[] packet, int packet_size)
  {
    //fill default fields:
    Version = 2;
    Padding = 0;
    Extension = 0;
    CC = 0;
    Marker = 0;
    Ssrc = 0;

    //check if total packet size is lower than the header size
    if (packet_size >= HEADER_SIZE) 
      {
	     //get the header bitsream:
	     header = new byte[HEADER_SIZE];
	     for (int i=0; i < HEADER_SIZE; i++)
	       header[i] = packet[i];

      	//get the payload bitstream:
      	payload_size = packet_size - HEADER_SIZE;
      	payload = new byte[payload_size];
      	for (int i=HEADER_SIZE; i < packet_size; i++)
      	  payload[i-HEADER_SIZE] = packet[i];

      	//interpret the changing fields of the header:
      	PayloadType = header[1] & 127;
      	SequenceNumber = unsigned_int(header[3]) + 256*unsigned_int(header[2]);
      	TimeStamp = unsigned_int(header[7]) + 256*unsigned_int(header[6]) + 65536*unsigned_int(header[5]) + 16777216*unsigned_int(header[4]);
      }
 }

  //--------------------------
  //getpayload: return the payload bistream of the RTPpacket and its size
  //--------------------------
  public int getpayload(byte[] data) {
    for (int i=0; i < payload_size; i++)
      data[i] = payload[i];
    return(payload_size);
  }

  //--------------------------
  //getpayload_length: return the length of the payload
  //--------------------------
  public int getpayload_length() {
    return(payload_size);
  }

  //--------------------------
  //getlength: return the total length of the RTP packet
  //--------------------------
  public int getlength() {
    return(payload_size + HEADER_SIZE);
  }

  //--------------------------
  //getpacket: returns the packet bitstream and its length
  //--------------------------
  public int getpacket(byte[] packet)
  {
    //construct the packet = header + payload
    for (int i=0; i < HEADER_SIZE; i++)
	packet[i] = header[i];
    for (int i=0; i < payload_size; i++)
	packet[i+HEADER_SIZE] = payload[i];

    //return total size of the packet
    return(payload_size + HEADER_SIZE);
  }

  //--------------------------
  //gettimestamp
  //--------------------------

  public int gettimestamp() {
    return(TimeStamp);
  }

  //--------------------------
  //getsequencenumber
  //--------------------------
  public int getsequencenumber() {
    return(SequenceNumber);
  }

  //--------------------------
  //getpayloadtype
  //--------------------------
  public int getpayloadtype() {
    return(PayloadType);
  }


  //--------------------------
  //print headers without the SSRC
  //--------------------------
  public void printheader()
  {

    for (int i=0; i < (HEADER_SIZE-4); i++)
      {
	for (int j = 7; j>=0 ; j--)
	  if (((1<<j) & header[i] ) != 0)
	    System.out.print("1");
	  else
	     System.out.print("0");
	     System.out.print(" ");
    }
    System.out.println();
  }

  //return the unsigned value of 8-bit integer nb
  static int unsigned_int(int nb) {
    if (nb >= 0)
      return(nb);
    else
      return(256+nb);
  }

}