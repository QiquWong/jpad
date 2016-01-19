package sandbox.adm;


class MyClass {
	
	public MyClass() {
	}
	
	public void myMethod() {
		System.out.println("myMethod :: Hello Λ²!");
	}
}

public class MyTest_Lambdas_01 {

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
	
	//---------------------------------------------------
	
	interface MyOperator {
	    void operate();
	}

	// Lambda function
	static MyOperator doOperateMyClass = () -> {
		System.out.println("doOperateMyClass :: Hello Λ! doOperateMyClass");
	};
	
	//---------------------------------------------------
	
	interface MyOperator2 {
	    void operate2(MyClass o);
	}

	// Lambda function
	static MyOperator2 doOperateMyClass2 = (o) -> {
		System.out.println("doOperateMyClass2 :: Hello Λ!!! doOperateMyClass");
		o.myMethod();
	};
	

	public MyTest_Lambdas_01() {
		// TODO Auto-generated constructor stub
	}

	//---------------------------------------------------

	void doExecute(MyOperator2 m, MyClass o) {
		m.operate2(o);
	}
	
	public static void main(String[] args) {

		doOperateMyClass.operate();
		
		MyClass myObj = new MyClass();
		doOperateMyClass2.operate2(myObj);
		
		System.out.println("-------------------");
		
		MyTest_Lambdas_01 m = new MyTest_Lambdas_01();
		m.doExecute(doOperateMyClass2, myObj);
		
		// doExecute         ==> doOpenTab [in event handler]
		// MyOperator2       ==> IopenInitiator [in MyProjectPane class]
		// doOperateMyClass2 ==> calls myTabbedPane.openFuselageInitiator() or myTabbedPane.openWingInitiator() ... 
		// myObj             ==> myTabbedPane
		/*
		 *  in MyProjectPane class:
		 * 
		 *  interface IopenInitiator {
		 *    void doOpen(MyTabbedPane myTabbedPane);
		 *  }
		 *  
		 *  void doOpenTab(IopenInitiator ii, MyTabbedPane myTabbedPane, MyComponent component) {
		 *    ...
		 *    ii.doOpen();
		 *    ...
		 *  }
		 *   
		 *   
		 *   // Lambda function
	         static IopenInitiator doOpenInitiator = (myTabbedPane, component) -> {
	           if (component instanceof MyFuselage) {
	             myTabbedPane.openFuselageInitiatorTab();
	           }
	           else if (component instanceof MyWing) {
	             myTabbedPane.openWingInitiatorTab();
	           }
	           
	           // ...
               
             };
             
             // ...

             // in double-click event handler
             doOpenTab(
                doOpenInitiator, // lambda function
                myTabbedPane,
                component
                );

		 *   
		 *     
		 * 
		 * 
		 * 
		 * 
		 */
		
	}

}
