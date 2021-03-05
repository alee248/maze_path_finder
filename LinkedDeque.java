// Solution by Mic Grigni.

// Small tricks to notice in this solution:
//  * removable field in Iter
//  * DIter is a subclass of Iter
//  * chained .append() calls in toString

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedDeque<Item> implements Deque<Item>
{
    // Not private, just to allow testing.
    int N;         // number of elements on deque
    Node first;    // beginning of deque
    Node last;     // end of deque

    // Linked list node.
    class Node
    {
        Item item;
        Node next;
        Node previous;          // new!
    }

    // Initialize empty deque
    public LinkedDeque()
    {
        first = null;
        last  = null;
        N = 0;
    }

    public boolean isEmpty() { return first == null; }

    public int size() { return N; }

    public void addLast(Item item)
    {
        Node oldlast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        last.previous = oldlast;
        if (isEmpty())
            first = last;
        else
            oldlast.next = last;
        ++N;
    }

    public void addFirst(Item item)
    {
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        first.next = oldfirst;
        if (last == null)
            last = first;
        else
            oldfirst.previous = first;
        ++N;
    }

    public Item removeFirst()
    {
        if (isEmpty())
            throw new NoSuchElementException();
        Item item = first.item;
        first = first.next;
        N--;
        if (isEmpty())
            last = null;
        else
            first.previous = null;
        return item;
    }

    public Item removeLast()
    {
        if (isEmpty()) throw new NoSuchElementException();
        Item item = last.item;
        last = last.previous;
        N--;
        if (last == null)
            first = null;
        else
            last.next = null;
        return item;
    }

    // The original slow version, kept for comparison:
    public String toStringSlow()
    {
        String s = "[", sep = "";
        for (Item item: this) {
            s += sep + item;
            sep = ", ";
        }
        return s + "]";
    }

    // Faster version, using a StringBuilder:
    public String toString()
    {
        StringBuilder sb = new StringBuilder("[");
        String sep = "";
        for (Item item: this) {
            sb.append(sep).append(item);
            sep = ", ";
        }
        return sb.append("]").toString();
    }

    // A standard iterator (visits items from first to last).
    public Iterator<Item> iterator() { return new Iter(); }

    private class Iter implements Iterator<Item>
    {
        Node current;           // has next item, or null
        Node removable;         // has removable item, or null
        Iter() { current = first; removable = null; }
        public boolean hasNext() { return current != null; }
        public void remove()
        {
            if (removable == null)
                throw new IllegalStateException();
            if (removable.previous == null)
                first = removable.next;
            else
                removable.previous.next = removable.next;
            if (removable.next == null)
                last = removable.previous;
            else
                removable.next.previous = removable.previous;
            --N;
            removable = null;
        }
        Node after(Node n) { return n.next; } // we override this
        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            removable = current;
            current = after(current);
            return removable.item;
        }
    }

    // A reverse iterator (visits items from last to first).
    public Iterator<Item> descendingIterator() { return new DIter(); }

    class DIter extends Iter {  // mostly inherits from Iter
        DIter() { current = last; removable = null; }
        Node after(Node n) { return n.previous; }
    }

    // There are some completely different ways to do this one.
    public void reverse()
    {
        if (N <= 1) return;
        // swap first and last
        Node t = first;
        first = last;
        last = t;
        // for each Node n, swap n.next and n.previous
        for (Node n=t; n != null; n=t) {
            t = n.next;
            n.next = n.previous;
            n.previous = t;
        }
    }
}
