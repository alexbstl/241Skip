//
// EVENTLIST.JAVA
// Skeleton code for your EventList collection type.
//
import java.util.*;


class EventList {

	Random randseq;
	int maxHeight;
	EventNode head;
	EventNode tail;

	////////////////////////////////////////////////////////////////////
	// Here's a suitable geometric random number generator for choosing
	// pillar heights.  We use Java's ability to generate random booleans
	// to simulate coin flips.
	////////////////////////////////////////////////////////////////////

	int randomHeight()
	{
		int v = 1;
		while (randseq.nextBoolean()) { v++; }
		return v;
	}


	//
	// Constructor
	//
	public EventList()
	{
		maxHeight=1;

		head= new EventNode(maxHeight);
		tail= new EventNode(0);
		tail.year=Integer.MAX_VALUE;
		head.year=Integer.MIN_VALUE;
		head.PointersGreater[maxHeight-1]=tail;

		randseq = new Random(58243); // You may seed the PRNG however you like.
	}


	/**
	 * This method adds an event to the EventList.  If another event from the given year has already been added to the EventList, then
	 * an EventNode for that year should already exist.  In this case, we simply add the Event to that EventNode.  If this is the first Event from the
	 * given year, we must create a new EventNode.  This method provides the infrastructure for allocating a new Node for the EventList skiplist.  
	 * We must first create the pillar height randomly for the new EventNode, and then ensure that the head has at least that height. We must also ensure that 
	 * every pointer in head above the previous max point initially points to the tail, so that our helper Methods work.  If our head is shorter than the new height,
	 * we double it with the method doubleHead() until it is larger than the new height.  After adding event to the EventNode, we first find the preceding 
	 * EventNode using the findRecentNodeHelper method, then iterate through our EventList starting at the highest pointer in the head node.  
	 * If the new node has a height greater than or equal to that of the pointer we are on, and the pointer points to a year
	 * after the year we are trying to insert, then we alter the pointers so that our node to insert points at what the node we are currently at points to
	 * and so that our current node points to the node we are inserting.  If our inserting node has a height less than what we are at, we drop a level and 
	 * retry the aboe process.  If we ever point to a year less than the year we are trying to insert after, we jump to that node.  The result of this process
	 * is that our new EventNode is forward-linked in our EventList at every level of its pointer array.
	 * @param e- an Event with a given year
	 */
	public void insert(Event e)
	{
		if(findNode(e.year)!=null){
			findNode(e.year).add(e);
		}else{
			int height = randomHeight();
			int oldHeight=maxHeight;
			if(height>maxHeight){
				while(head.height<height){
					doubleHead(oldHeight);
				}
				maxHeight=height;
			}
			EventNode add = new EventNode(height);
			add.year=e.year;
			add.add(e);
			height=maxHeight-1;
			EventNode currentNode=head;
			EventNode prevNode=findRecentNodeHelper(e.year);
			while(height>=0){
				if(currentNode.PointersGreater[height].year==prevNode.year){
					currentNode=currentNode.PointersGreater[height];
					if(add.height<=currentNode.height){
						for(int i = 0; i<add.height;i++){
							add.PointersGreater[i]=currentNode.PointersGreater[i];
							currentNode.PointersGreater[i]=add;	
						}
					}else{
						for(int i = 0; i<currentNode.height;i++){
							add.PointersGreater[i]=currentNode.PointersGreater[i];
							currentNode.PointersGreater[i]=add;
						}
					}
					return;
				}else if(currentNode.PointersGreater[height].year>prevNode.year){
					if(height<=add.height-1){
						add.PointersGreater[height]=currentNode.PointersGreater[height];
						currentNode.PointersGreater[height]=add;
					}
					height--;
				}else{
					currentNode=currentNode.PointersGreater[height];
				}
			}
		}
	}

	/**
	 * This method doubles the height of the head EventNode in order to actively resize the Head as the list gets larger.  It works by taking the old
	 * height (i.e.  the pre-doubled maxHeight) and creating a new EventNode with a height twice that size.  We then duplicate all of the previous head's
	 * pointers into that EventNode's PointersGreater array, and set all the rest of the slots in that array point to the tail.
	 * @param oldHeight
	 */
	private void doubleHead(int oldHeight){

		EventNode a = new EventNode(2*head.height);
		a.year=head.year;
		for(int i =0; i<a.height;i++){
			if(i<oldHeight){
				a.PointersGreater[i]=head.PointersGreater[i];
			}else{
				a.PointersGreater[i]=tail;
			}
		}
		head=a;
	}

	/**
	 * This removes a year and all of it's events from our EventList.  We accomplish this by unlinking the node from our EventList.  We start at the head
	 * and through a method similar to our "find" methods find every link pointing to the unique EventNode with the year we would like it to remove.  For
	 * every pointer we find, we simply have it point to the next year linked at that level.
	 * @param year
	 */
	public void remove(int year)
	{
		int height=maxHeight-1;
		EventNode currentNode=head;
		while(height>=0){
			if(currentNode.PointersGreater[height].year>year){
				height--;
			}else if(currentNode.PointersGreater[height].year==year){
				EventNode remove=currentNode.PointersGreater[height];
				currentNode.PointersGreater[height]=remove.PointersGreater[height];
				height--;
			}else{
				currentNode=currentNode.PointersGreater[height];
			}
		}
	}


	/**
	 * This method either finds all the events for a given year or, if that year doesn't exist, from the most recent predecessor to that year.  It works
	 * by using the findRecentNodeHelper method to find the event node immediately preceding the year for the node we are looking for.  We then check the level
	 * 0 pointer and, if that returns the year in the parameter, we return the events from that year.  Otherwise we return the events for the node found by
	 * the helper method.
	 * @param year
	 * @return an array of events or null if no earlier dates contain events.
	 */
	public Event [] findMostRecent(int year)
	{
		EventNode currentNode=findRecentNodeHelper(year);
		if(currentNode.PointersGreater[0].year==year){
			return currentNode.PointersGreater[0].toArray();
		}
		if(currentNode.Events.isEmpty()){
			return null;
		}
		return currentNode.toArray();
	}

	/**
	 * This method allows us to search for the node preceding the year we are given .
	 * It works by beginning at the specified maximum height, and iterating through the list, dropping pointer levels if the year of that node is
	 * greater than or equal to the year we are looking for.  We eventually return the node whose level 0 pointer points to the input year, i.e. the last year
	 * in the list before the pointer year
	 * @param year
	 * @return the most recent node before the year provided
	 */
	private EventNode findRecentNodeHelper(int year){
		EventNode currentNode=head;
		int height=maxHeight-1;
		while(height>=0){
			if (currentNode.PointersGreater[height].year>=year){
				height--;
			}else{
				currentNode=currentNode.PointersGreater[height];
			}
		}
		return currentNode;
	}
	/**
	 * This method finds all events in the range of events by first finding the node preceding the first year.  We then advance our iterator and add all 
	 * the events from the Events ArrayList for that node to an arraylist.  We continue this processing, advancing our current year along the 0th pointer
	 * until we reach the year specified by the "last" parameter.  We then add these nodes to the arrayList, and return the ArrayList's toArray.  This will
	 * always be chronologically sorted as long as our EventNodes are inserted properly.
	 * @param first
	 * @param last
	 * @return an array of all events in our events list between the specified years, sorted chronologically by year or null if no events exist in that range
	 */
	public Event [] findRange(int first, int last)
	{
		EventNode currentNode=findRecentNodeHelper(first);
		ArrayList<Event> returnEvents= new ArrayList<Event>();
		if(currentNode.year<first){
			currentNode=currentNode.PointersGreater[0];
		}

		while(currentNode.year<=last){

			returnEvents.addAll(currentNode.Events);
			currentNode=currentNode.PointersGreater[0];
		}
		if(returnEvents.isEmpty()){
			return null;
		}
		Event [] returnEvent= new Event[returnEvents.size()];
		return returnEvents.toArray(returnEvent);

	}
	/**
	 * This helper method returns either the node associated with a year or null if that node doesn't exist.
	 * It searches the skiplist starting with the highest level pointer at the head of the list (at an array index of maxheight-1, and drops 
	 * to the next level if the year of the node the current node points to at its current level is greater than the year we are searchign for
	 * This terminates either when the lowest level pointer points to a year greater than we are searching for (And the node we are at is 
	 * less than that year), in which case we return null or when we find the correct node, in which case we return that node.
	 * @param year- the year of the node to return
	 * @return
	 */
	private EventNode findNode(int year){

		int height = maxHeight-1;
		EventNode currentNode=head;
		while(height>=0){
			if(currentNode.year==year){
				return currentNode;
			}
			if(currentNode.equals(tail)){
				return null;
			}
			if(currentNode.PointersGreater[height].year>year){
				height--;
			}else{
				currentNode=currentNode.PointersGreater[height];
			}
		}
		return null;
	}
}
