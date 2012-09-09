package pl.rtshadow.jtriss.column.accessor;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.rtshadow.jtriss.column.accessor.ListColumnAccessor.generator;
import static pl.rtshadow.jtriss.test.TestColumnElement.chain;
import static pl.rtshadow.jtriss.test.TestColumnElement.element;
import static pl.rtshadow.jtriss.test.TestObjects.TEST_COLUMN_ID;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import pl.rtshadow.jtriss.column.element.ColumnElement;
import pl.rtshadow.jtriss.column.element.ModifiableColumnElement;
import pl.rtshadow.jtriss.test.TestColumnElement;
import pl.rtshadow.jtriss.test.TestFactory;

@RunWith(MockitoJUnitRunner.class)
public class ListColumnAccessorTest extends AbstractColumnAccessorTest {
  @Before
  public void setUp() {
    accessorGenerator = generator(Integer.class, constructor);
    accessorGenerator.setFactory(new TestFactory());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void returnsTwoElementsFromTheSameColumn() {
    when(column.contains(any(ColumnElement.class))).thenReturn(false, true, false);

    ReconstructedObject<Integer> reconstructed = reconstruct(testList());

    assertReconstructedContainsOnly(reconstructed.getObject(), 6, 7, 8);
    assertThat(reconstructed.getNextElementInRow().getValue()).isEqualTo(9);
  }

  private void assertReconstructedContainsOnly(Object reconstructed, Integer... elements) {
    assertThat(reconstructed).isInstanceOf(List.class);
    List<Integer> objects = (List<Integer>) reconstructed;

    assertThat(objects).containsOnly(elements);
  }

  private TestColumnElement testList() {
    return chain(
        element(6).atPosition(0),
        element(7).atPosition(1),
        element(8).atPosition(2), element(9).inColumn(TEST_COLUMN_ID + 1));
  }

  @Test
  public void reconstructsOnlyWhenStartedFromFirst() {
    when(column.contains(any(ColumnElement.class))).thenReturn(true);

    ColumnAccessor<Integer> accessor = accessorGenerator.prepareColumnAccessor();
    accessor.prepareMainColumnForReconstruction();
    accessor.reconstruct(testList().getNextElementInTheRow());

    assertThat(accessor.reconstruct(testList())).isNull();

    accessor.finishReconstruction();
    assertReconstructedContainsOnly(
        accessor.reconstruct(testList()).getObject(),
        6, 7, 8);
  }

  @Test
  public void returnsNullIfListNotIncludedInColumn() {
    when(column.contains(any(ColumnElement.class))).thenReturn(false);

    assertThat(reconstruct(testList())).isNull();
  }

  private ReconstructedObject<Integer> reconstruct(TestColumnElement element) {
    ColumnAccessor<Integer> accessor = accessorGenerator.prepareColumnAccessor();
    return accessor.reconstruct(element);
  }

  @Test
  public void addsEachObjectInGivenList() {
    ModifiableColumnElement<Integer> newElement = accessorGenerator.insert(asList(7, 8), element(9));

    assertThat(newElement).isEqualTo(element(8).withNext(element(9)));
    verify(constructor).add(chain(element(7), element(8), element(9)));
    verify(constructor).add(element(8).withNext(element(9)));
  }
}
