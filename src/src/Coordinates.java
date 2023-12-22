public record Coordinates(int row, int column) {
        public Coordinates(String coordinates){
            this(coordinates.charAt(0) - 'A', Integer.parseInt(coordinates.substring(1)) - 1);
        }
        public Coordinates(int index){
            this(index / 10, index  % 10);
        }
}
