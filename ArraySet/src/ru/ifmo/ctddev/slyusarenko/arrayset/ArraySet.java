package ru.ifmo.ctddev.slyusarenko.arrayset;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by maksim on 25.02.15.
 */
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet <E>  {

    private E[] array;
    private Comparator<? super E> comparator;
    private boolean defaultComparator;
    int left, right;
    boolean reversed;

    public ArraySet() {
        this(null, null);
    }

    private ArraySet(int left, int right, boolean reversed, E[] array, Comparator<? super E> comparator, boolean defaultComparator) {
        this.left = left;
        this.right = right;
        this.reversed = reversed;
        this.array = array;
        this.comparator = comparator;
        this.defaultComparator = defaultComparator;
    }

    private E[] deleteTheSame(Collection<E> collection) {
        if (collection.size() == 0) {
            return (E[]) new Object[0];
        }
        int size = 0;
        for (E e: collection) {
            if (e != null) {
                size++;
            }
        }
        E[] tmp = (E[]) new Object[size];
        int q = 0;
        for (E e: collection) {
            if (e != null) {
                tmp[q] = e;
                q++;
            }
        }
        int differentValues = 0;
        Arrays.sort(tmp, comparator);
        for (int i = 0; i < tmp.length - 1; i++) {
            if (comparator.compare(tmp[i + 1], tmp[i]) != 0) {
                differentValues++;
            }
        }
        if (tmp.length == 1 || comparator.compare(tmp[tmp.length - 1], tmp[tmp.length - 2]) != 0) {
            differentValues++;
        }
        if (differentValues == 0) {
            E[] answer = (E[]) new Object[1];
            answer[0] = tmp[0];
            return answer;
        }
        int k = 0;
        E[] answer = (E[]) new Object[differentValues];
        for (int i = 0; i < tmp.length - 1; i++) {
            if (comparator.compare(tmp[i + 1], tmp[i]) != 0) {
                answer[k] = tmp[i];
                k++;
            }
        }
        if (tmp.length == 1 || comparator.compare(tmp[tmp.length - 1], tmp[tmp.length - 2]) != 0) {
            answer[k] = tmp[tmp.length - 1];
        }
        return answer;
    }

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator) {
        if (comparator != null) {
            this.comparator = comparator;
            defaultComparator = false;
        } else {
            this.comparator = new Comparator<E>() {
                @Override
                public int compare(E o1, E o2) {
                    if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        Comparable tmp1 = (Comparable) o1;
                        Comparable tmp2 = (Comparable) o2;
                        return tmp1.compareTo(tmp2);
                    } else {
                        return 0;
                    }
                }
            };
            defaultComparator = true;
        }
        if (collection != null) {
            array = deleteTheSame(collection);
            reversed = false;
            left = 0;
            right = array.length;
        } else {
            array = (E[]) new Object[0];
        }
        reversed = false;
        left = 0;
        right = array.length;
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    private int binarySearch(E elementToFind, int numberOfOperation) {
        if (right - left == 0) {
            return -2;
        }
        int l, r;
        l = left - 1;
        r = right;
        while (r > l + 1) {
            int m = (l + r) / 2;
            if (comparator.compare(array[m], elementToFind) >= 0) {
                r = m;
                if (comparator.compare(array[m], elementToFind) == 0) {
                    break;
                }
            } else {
                l = m;
            }
        }
        if (numberOfOperation == 0 || numberOfOperation == 1) {
            if (r == left - 1) {
                return -2;
            } else if (r == right) {
                r--;
                return r;
            }
            if ((numberOfOperation == 0 && comparator.compare(array[r], elementToFind) >= 0) || (numberOfOperation == 1 && comparator.compare(array[r], elementToFind) > 0)) {
                r--;
            }
            if (r == left - 1) {
                return -2;
            }
        } else if (numberOfOperation == 2 || numberOfOperation == 3) {
            if (r == right) {
                return -2;
            } else if (r == left - 1) {
                r++;
            }
            if ((numberOfOperation == 3 && comparator.compare(array[r], elementToFind) <= 0) || (numberOfOperation == 2 && comparator.compare(array[r], elementToFind) < 0)) {
                r++;
            }
            if (r == right) {
                return -2;
            }
        }
        if (r == left - 1) {
            return -2;
        }
        return r;
    }

    @Override
    public E lower(E e) {
        int tmp = binarySearch(e, 0);
        if (tmp == -2) {
            return null;
        }
        return array[tmp];
    }

    @Override
    public E floor(E e) {
        int tmp = binarySearch(e, 1);
        if (tmp == -2) {
            return null;
        }
        return array[tmp];
    }

    @Override
    public E ceiling(E e) {
        int tmp = binarySearch(e, 2);
        if (tmp == -2) {
            return null;
        }
        return array[tmp];
    }

    @Override
    public E higher(E e) {
        int tmp = binarySearch(e, 3);
        if (tmp == -2) {
            return null;
        }
        return array[tmp];
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("No remove");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("No remove");
    }

    @Override
    public int size() {
        return right - left;
    }

    @Override
    public boolean contains(Object o) {
        int tmp = Arrays.binarySearch(array, (E) o, comparator);
        return (tmp >= left && tmp < right && comparator.compare(array[tmp], (E) o) == 0);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int count = right - left;
            private int index = 0;
            @Override
            public boolean hasNext() {
                return ((index < count && !reversed) || (index > 0 && reversed));
            }

            @Override
            public E next() {
                if (!reversed) {
                    if (index < count) {
                        return array[index++];
                    } else {
                        throw new NoSuchElementException("No such element");
                    }
                } else {
                    if (index > left) {
                        return array[index--];
                    } else {
                        throw new NoSuchElementException("No such element");
                    }
                }
            }
        };
    }

    @Override
    public Object[] toArray() {
        E[] cur = (E[]) new Comparable[right - left];
        System.arraycopy(array, left, cur, 0, right - left);
        return cur;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("No add");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("No remove");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("No add");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("No retain");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("No remove");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("No clear");
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<E>(this.left, this.right, !this.reversed, this.array, this.comparator.reversed(), this.defaultComparator);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Iterator<E>() {
            private int index = right - 1;
            @Override
            public boolean hasNext() {
                return ((index > left && !reversed) || (index < right - 1 && reversed));
            }

            @Override
            public E next() {
                if (!reversed) {
                    if (index > left) {
                        return array[index--];
                    } else {
                        throw new NoSuchElementException("No such element");
                    }
                } else {
                    if (index < right - 1) {
                        return array[index++];
                    } else {
                        throw new UnsupportedOperationException("No such element");
                    }
                }
            }
        };
    }

    private int findLeft(E fromElement, boolean inclusive) {
        int ans;
        if (!inclusive) {
            ans = binarySearch(fromElement, 3);
            if (ans == -2) {
                ans = right;
            }
        } else {
            ans = binarySearch(fromElement, 2);
            if (ans == -2) {
                ans = right;
            }
        }
        return ans;
    }

    private int findRight(E toElement, boolean inclusive) {
        int ans;
        if (!inclusive) {
            ans = binarySearch(toElement, 0);
            if (ans == -2) {
                ans = left;
                return ans;
            }
            ans++;
        } else {
            ans = binarySearch(toElement, 1);
            if (ans == -2) {
                ans = left;
                return ans;
            }
            ans++;
        }
        return ans;
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int left = findLeft(fromElement, fromInclusive);
        int right = findRight(toElement, toInclusive);
        /*if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Wrong from and to elements");
        }*/
        if (left > right) {
            right = left;
        }
        return new ArraySet<E>(left, right, this.reversed, this.array, this.comparator, this.defaultComparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (left == right) {
            return this;
        } else {
            return subSet(first(), true, toElement, inclusive);
        }
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (left == right) {
            return this;
        } else {
            return subSet(fromElement, inclusive, last(), true);
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        if (defaultComparator) {
            return  null;
        }
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (right - left == 0) {
            throw new NoSuchElementException("Empty set");
        }
        if (!reversed) {
            return array[left];
        } else {
            return array[right - 1];
        }
    }

    @Override
    public E last() {
        if (right - left == 0) {
            throw new NoSuchElementException("Empty set");
        }
        if (!reversed) {
            return array[right - 1];
        } else {
            return array[left];
        }
    }

}
