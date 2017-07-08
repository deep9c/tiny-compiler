package cop5556sp17;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	
	//TODO  add fields
	class SymTabAttributes{
		int scope;
		Dec declaration;
		
		public SymTabAttributes(int scope, Dec declaration){
			this.scope=scope;
			this.declaration=declaration;
		}		
	}
	
	//ArrayList<SymTabAttributes> attributeList = new ArrayList<SymTabAttributes>();
	HashMap<String, ArrayList<SymTabAttributes>> tableEntry;
	Stack<Integer> scope_stack;
	int  current_scope, next_scope;
	
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		current_scope = next_scope;
		next_scope++;
		scope_stack.push(current_scope);
		//System.out.println("enter scope: pushed "+current_scope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		scope_stack.pop();
		current_scope = scope_stack.peek();
		//System.out.println("leave scope: popped "+current_scope);
	}
	
	public boolean insert(String ident, Dec dec){
		//System.out.println("symtab insert:"+ident);
		if(tableEntry.containsKey(ident)){
			ArrayList<SymTabAttributes> attributeList = tableEntry.get(ident);
			//for(SymTabAttributes listEntry : attributeList){
			for(int i=0; i<attributeList.size(); i++){ 		//check if variable already declared in current scope
				SymTabAttributes listEntry = attributeList.get(i);
				//System.out.println("found in symtab insert:"+ident+"..listentry.scope="+listEntry.scope+"..current_scope="+current_scope);
				if(listEntry.scope==current_scope){
					return false;	
				}
			}
			attributeList.add(new SymTabAttributes(current_scope,dec));	//table entry found and variable not declared in current scope
			return true;
		}
		
		ArrayList<SymTabAttributes> newAttributeList = new ArrayList<SymTabAttributes>();
		SymTabAttributes newAttribute = new SymTabAttributes(current_scope,dec);
		newAttributeList.add(newAttribute);
		tableEntry.put(ident, newAttributeList);
		//System.out.println("symtab inserted: "+ident+"..current_scope="+current_scope);
		return true;
	}
	
	public Dec lookup(String ident){
		if(tableEntry.containsKey(ident)){
			ArrayList<SymTabAttributes> attributeList = tableEntry.get(ident);
			Dec decOfIdent=null;
			
			//for(SymTabAttributes listEntry : attributeList){
			//for(int i=0; i<attributeList.size(); i++){
			for(int i=attributeList.size()-1; i>=0; i--){
				SymTabAttributes listEntry = attributeList.get(i);
				//System.out.println("lookup:"+ident+"..listEntry.scope="+listEntry.scope);
				//if(listEntry.scope<=current_scope){
					if(scope_stack.contains(listEntry.scope)){
						decOfIdent=listEntry.declaration;
						break;
					}
				//}
				//else{
				//	break;
				//}
			}
			return decOfIdent;
		}		
		return null;
	}
		
	public SymbolTable() {
		this.tableEntry = new HashMap<String, ArrayList<SymTabAttributes>>();
		this.scope_stack = new Stack<Integer>();
		this.current_scope=0;
		this.scope_stack.push(this.current_scope);
		this.next_scope=1;
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "CurrentScope="+current_scope;
	}
	
	


}
