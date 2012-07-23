package pl.rtshadow.jtriss.column.accessor;

import static org.apache.commons.lang3.BooleanUtils.negate;
import static pl.rtshadow.jtriss.column.element.ColumnElementFactory.createElement;

import java.util.ArrayList;
import java.util.List;

import pl.rtshadow.jtriss.column.ColumnConstructor;
import pl.rtshadow.jtriss.column.SortedColumn;
import pl.rtshadow.jtriss.column.element.ColumnElement;
import pl.rtshadow.jtriss.column.element.ModifiableColumnElement;

public class ListColumnAccessor implements ColumnAccessor {
  public static ListColumnAccessor INSTANCE = new ListColumnAccessor();

  private ListColumnAccessor() {
  }

  @Override
  public <T extends Comparable<? super T>> ReconstructedObject<T> reconstruct(ColumnElement<T> firstElement, SortedColumn<T> column) {
    ReconstructedObject<T> reconstructed = null;
    ArrayList<T> valuesList = new ArrayList<T>();
    while (column.contains(firstElement)) {
      if (reconstructed == null) {
        reconstructed = new ReconstructedObject<T>(valuesList, null);
      }
      valuesList.add(firstElement.getValue());
      reconstructed.setNextElementInRow(firstElement.getNextElementInTheRow());
      firstElement = firstElement.getNextElementInTheRow();
    }
    return reconstructed;
  }

  @Override
  public <T extends Comparable<? super T>>
      ModifiableColumnElement<T> insert(Object value, ColumnElement<T> nextElement, ColumnConstructor<T> constructor) {
    ModifiableColumnElement<T> lastCreatedElement = null;

    List<T> valuesList = retrieveNonEmptyListFromObject(value);
    for (T singleValue : valuesList) {
      ModifiableColumnElement<T> currentElement = createElement(singleValue);
      if (lastCreatedElement != null) {
        lastCreatedElement.setNextElement(currentElement);
      }
      constructor.add(currentElement);
      lastCreatedElement = currentElement;
    }
    lastCreatedElement.setNextElement(nextElement);

    return lastCreatedElement;
  }

  private <T> List<T> retrieveNonEmptyListFromObject(Object value) {
    if (negate(value instanceof List)) {
      throw new IllegalArgumentException("Expected instance of java.util.List, got: " + value.getClass());
    }

    @SuppressWarnings("unchecked")
    List<T> valuesList = (List<T>) value;

    if (valuesList.isEmpty()) {
      throw new UnsupportedOperationException("Empty lists are not yet supported");
    }

    return valuesList;
  }

}
