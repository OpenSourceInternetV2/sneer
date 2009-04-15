package sneer.pulp.reactive.collections.impl;

import sneer.pulp.reactive.collections.ListChange;

final class ListElementMoved<T> extends AbstractListValueChange<T> implements ListChange<T> {

	private final int _newIndex;

	ListElementMoved(int oldIndex, int newIndex, T element) {
		super(oldIndex, element);
		_newIndex = newIndex;
	}

	@Override
	public void accept(Visitor<T> visitor) {
		visitor.elementMoved(_index, _newIndex, _element);
	}
}