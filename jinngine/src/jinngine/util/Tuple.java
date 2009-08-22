package jinngine.util;

public class Tuple<T,U> {
	public  T first;
	public  U second;
	public Tuple(T first, U second) {
		super();
		this.first = first;
		this.second = second;
	}
	
	  @SuppressWarnings("unchecked")
	public boolean equals( Object other ) {
		    return this.first.hashCode() == ((Tuple<T,U>)other).first.hashCode()
		    && this.second.hashCode() == ((Tuple<T,U>)other).second.hashCode();
	  }
}
