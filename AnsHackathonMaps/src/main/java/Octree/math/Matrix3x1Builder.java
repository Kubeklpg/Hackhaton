package Octree.math;

/**
 * Implementation of a 3x1 matrix. This class has been
 * generated for optimal performance.
 */
final class Matrix3x1Builder extends MatrixBuilder<Matrix3x1> {

  /**
   * Value of row 0, column 0.
   */
  private double r0c0;

  /**
   * Value of row 1, column 0.
   */
  private double r1c0;

  /**
   * Value of row 2, column 0.
   */
  private double r2c0;


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
      case 2:
        switch (column) {
          case 0: return this.r2c0;
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
      case 2:
        switch (column) {
          case 0:
            this.r2c0 = value;
            break;
          default: throw new IllegalArgumentException("No such column: " + column);
        }
        break;
      default: throw new IllegalArgumentException("No such row: " + row);
    }
  }

  /**
   * Construct a new Matrix3x1 instance.
   */
  public Matrix3x1 toMatrix() {
    return new Matrix3x1(
      this.r0c0,
      this.r1c0,
      this.r2c0
    );
  }
}