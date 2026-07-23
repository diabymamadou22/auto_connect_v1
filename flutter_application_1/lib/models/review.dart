class Review {
  final String id;
  final String serviceId;
  final String userName;
  final String comment;
  final double rating;
  final DateTime createdAt;

  Review({
    required this.id,
    required this.serviceId,
    required this.userName,
    required this.comment,
    required this.rating,
    required this.createdAt,
  });
}
