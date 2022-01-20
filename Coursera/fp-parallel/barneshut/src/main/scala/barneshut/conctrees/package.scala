package barneshut.conctrees

import org.scalameter.*

extension [T] (self: Conc[T])
  def foreach[U](f: T => U): Unit = Conc.traverse(self, f)
  def <>(that: Conc[T]) = Conc.concatTop(self.normalized, that.normalized)
