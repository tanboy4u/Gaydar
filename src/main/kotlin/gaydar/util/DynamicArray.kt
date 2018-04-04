package gaydar.util

import java.util.*

class DynamicArray<T : Any?>(size : Int) : Iterable<T>
{
  constructor(cap : Int, size : Int) : this(cap)
  {
    resize(size)
  }

  private var elementData = Array<Any?>(size) { null }

  var size : Int = size
    private set(value)
    {
      field = value
    }

  operator fun get(index : Int) : T
  {
    rangeCheck(index)
    return elementData[index] as T
  }

  operator fun set(index : Int, element : T)
  {
    rangeCheck(index)
    elementData[index] = element
  }

  fun rawGet(index : Int) : T
  {
    if (index >= elementData.size)
      throw IndexOutOfBoundsException()
    return elementData[index] as T
  }

  fun resize(newSize : Int) : DynamicArray<T>
  {
    size = newSize
    ensure(newSize)
    return this
  }

  private fun rangeCheck(index : Int)
  {
    if (index >= size)
      throw IndexOutOfBoundsException()
  }

  private fun ensure(minCapacity : Int)
  {
    if (minCapacity > elementData.size)
    {
      val oldCapacity = elementData.size
      var newCapacity = oldCapacity + (oldCapacity shr 1)
      if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity
      elementData = Arrays.copyOf(elementData, newCapacity)
    }
  }

  override fun iterator() = object : Iterator<T>
  {
    val data = elementData
    val _size = size
    var i = 0
    override fun hasNext() = i < _size

    override fun next() = data[i++] as T
  }
}