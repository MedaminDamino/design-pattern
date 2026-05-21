package model;

/**
 * Enum of all drawable shape types.
 * Open/Closed Principle: add a new type here + a model class + factory case.
 * Nothing else in the architecture needs to change.
 */
public enum ShapeType {
    // ── Basic ────────────────────────────────────────
    RECTANGLE, CIRCLE, LINE,
    // ── Polygons ─────────────────────────────────────
    TRIANGLE, ELLIPSE, PENTAGON, HEXAGON, DIAMOND, STAR, ARROW, TRAPEZOID,
    // ── Graph ────────────────────────────────────────
    NODE, EDGE,
    // ── Tools ────────────────────────────────────────
    FILL
}
