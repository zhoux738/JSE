/*
MIT License

Copyright (c) 2017 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.clapp.repl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HistoryManager {
	
	private static final int MAX = 500;
	
	private int index = 0;
	private List<String> all = new ArrayList<String>(MAX);

	public void add(String str){
		int rem = MAX - all.size();
		if (rem == 1) {
			// About the hit the capacity. Transfer the storage to a linked list to enhance efficiency on
			// removing the leading element, as we will do that every time we add a new entry in the future.
			List<String> all2 = new LinkedList<String>();
			all2.addAll(all);
			all = all2;
		} else if (rem < 1){
			all.remove(0);
		}
		
		all.add(str);
	}
	
	/**
	 * Returns a list of historical commands in chronological order.
	 * 
	 * @return A list of historical commands in chronological order.
	 */
	public List<String> list(){
		return all;
	}
	
	/**
	 * Get the previous history, starting with the top (latest). This method
	 * is based on an internal state of the history manager called navigation 
	 * index, which can be reset by {@link #reset()}.
	 * 
	 * @return null if reaching the bottom of history.
	 */
	public String prev(){
		index--;
		if (index >= 0){
			return all.get(index);
		} else {
			index = 0;
		}
		
		return null;
	}
	
	public String next(){
		index++;
		if (index < all.size()){
			return all.get(index);
		} else {
			index = all.size();
			if (index > 0) {
				index--;
			}
		}
		
		return null;
	}
	
	/**
	 * Reset the navigation index to top. Note this is NOT clearing history records.  
	 */
	public void reset(){
		index = all.size();
	}
}
