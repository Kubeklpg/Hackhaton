package Octree.math;

/**
 * Implementation of a 2x1 matrix. This class has been
 * generated for optimal performance.
 */
final class Matrix2x1Builder extends MatrixBuilder<Matrix2x1> {

  /**
   * Value of row 0, column 0.
   */
  private double r0c0;

  /**
   * Value of row 1, column 0.
   */
  private double r1c0;


  /**
   * Return the value of the specified entry.
   */
  @Override
  public double get(int row, int column) {
    switch (row) {

      case 0:
        switch (column) {
          case 0: return this.r0c0;
          default: throw new IllegalArgumentException("No such column: " + column);
        }
      case 1:
        switch (column) {
          case 0: return this.r1c0;
          default: throw new IllegalArgumentException("No such column: " + column);
        }
      default: throw new IllegalArgumentException("No such row: " + row);
    }
  }

  /**
   * Set the value of an entry.
   */
  @Override
  public void set(int row, int column, double value) {
    switch (row) {

      case 0:
        switch (column) {
          case 0:
            this.r0c0 = value;
            break;
          default: throw new IllegalArgumentException("No such column: " + column);
        }
        break;
      case 1:
        switch (column) {
          case 0:
            this.r1c0 = value;
            break;
          default: throw new IllegalArgumentException("No such column: " + column);
        }
        break;
      default: throw new IllegalArgumentException("No such row: " + row);
    }
  }

  /**
   * Construct a new Matrix2x1 instance.
   */
  public Matrix2x1 toMatrix() {
    return new Matrix2x1(
      this.r0c0,
      this.r1c0
    );
  }
}