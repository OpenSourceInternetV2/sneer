package sneer.pulp.reactive.collections.impl;

import sneer.pulp.reactive.collections.ListChange;

final class ListElementRemoved<T> extends AbstractListValueChange<T> implements ListChange<T> {

	ListElementRemoved(int index, T element) {
		super(index, element);
	}

	@Override
	public void accept(Visitor<T> visitor) {
		visitor.elementRemoved(_index, _element);
	}
}