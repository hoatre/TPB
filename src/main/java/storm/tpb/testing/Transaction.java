package storm.tpb.testing;

public class Transaction {
	   private String trx_id;
	   private String trx_code;
	   private String ch_id;
	   private Integer amount;
	   private String acc_no;
	   private String prd_id;
	   private Long timestamp;
	   public Transaction(){
	 
	   }

	   public Transaction(String trx_id, String trx_code, String ch_id, Integer amount,String acc_no,String prd_id,Long timestamp){
	      this.trx_id = trx_id;
	      this.trx_code = trx_code;
	      this.ch_id = ch_id;
	      this.amount = amount;
	      this.acc_no = acc_no;
		   this.prd_id = prd_id;
		   this.timestamp = timestamp;
	   }
	   public String gettrx_id()
	   {
	      return trx_id;
	   }
	   public void settrx_id(String trx_id)
	   {
	      this.trx_id = trx_id;
	   }
	   
	   public String gettrx_code()
	   {
	      return trx_code;
	   }
	   public void settrx_code(String trx_code)
	   {
	      this.trx_code = trx_code;
	   }
	   
	   public String getch_id()
	   {
	      return ch_id;
	   }
	   public void setch_id(String ch_id)
	   {
	      this.ch_id = ch_id;
	   }
	   
	   public Integer getamount()
	   {
	      return amount;
	   }
	   public void setamount(Integer amount)
	   {
	      this.amount = amount;
	   }
	   
	   public String getacc_no()
	   {
	      return acc_no;
	   }
	   public void setacc_no(String acc_no)
	   {
	      this.acc_no = acc_no;
	   }
	   
	   public String getprd_id()
	   {
	      return prd_id;
	   }
	   public void setprd_id(String prd_id)
	   {
	      this.prd_id = prd_id;
	   }

		public Long gettimetamp()
		{
			return timestamp;
		}
		public void settimetamp(Long timetamp)
		{
			this.timestamp = timestamp;
		}
}
