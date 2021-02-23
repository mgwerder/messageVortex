package net.messagevortex.asn1;

enum OperationType {
  SPLIT_PAYLOAD(150, new SplitPayloadOperation()),
  MERGE_PAYLOAD(160, new MergePayloadOperation()),
  ENCRYPT_PAYLOAD(300, new EncryptPayloadOperation()),
  DECRYPT_PAYLOAD(310, new DecryptPayloadOperation()),
  ADD_REDUNDANCY(400, new AddRedundancyOperation()),
  REMOVE_REDUNDANCY(410, new RemoveRedundancyOperation()),
  MAP(1001, new MapBlockOperation());

  int id;
  Operation operation;

  OperationType(int id, Operation operation) {
    this.id = id;
    this.operation = operation;
  }

  int getId() {
    return id;
  }

  Operation getFactory() {
    return operation;
  }

  /***
   * <p>Look up an algorithm by id.</p>
   *
   * @param id     the idto be looked up
   * @return the algorithm or null if not known
   */
  public static OperationType getById(int id) {
    for (OperationType e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }
}
