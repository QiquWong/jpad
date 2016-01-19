package sandbox.adm;

public class MyTest_Lambdas_00 {

	interface GenericOperator<T> {
	    T operate(T ... operands);
	}
	
	// Lambda function
	static GenericOperator<Integer> multiply = numbers -> {
	  int result = 1;
	  for(int num : numbers)
	      result *= num;
	  return result;
	};

	// Lambda function
	static GenericOperator<Integer> multiply2 = numbers -> {
	  int result = 1;
	  for(int num : numbers)
	      result *= -num;
	  return result;
	};
	
	public MyTest_Lambdas_00() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello Λ!");
		System.out.println("Multiplication result: " + multiply.operate(2,3,4));
		System.out.println("Multiplication 2 result: " + multiply2.operate(-2,3,-4));
	}

}
