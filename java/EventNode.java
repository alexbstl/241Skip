import java.util.ArrayList;
/**
 * This class specifies an EventNode for the EventList Interface.  Each EventNode contains an array of pointers to other EventNodes and an ArrayList of Events.
 * Each event must happen within a specific year.  For simplicity, we also include the height of the pointers array and the year of the EventNode.  This class
 * also provides a method to add Events to the year, and to create an array of all events that take place in the given year.
 * @author Alex Bernstein
 *
 */
public class EventNode {

	ArrayList<Event> Events= new ArrayList<Event>();
	EventNode PointersGreater[];
	ArrayList<String> stringout;
	int year;
	int height;

	public EventNode(int height){
		PointersGreater = new EventNode[height];
		this.height=height;

	}
	public void add(Event e){
		Events.add(e);
	}
	public Event[] toArray(){
		return Events.toArray(new Event[Events.size()]);
	}
}
