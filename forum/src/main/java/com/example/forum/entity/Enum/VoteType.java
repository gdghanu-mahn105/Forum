package com.example.forum.entity.Enum;

public enum VoteType {
        UPVOTE(1),
        DOWNVOTE(-1),
        NONE(0);

        private final int value;

        VoteType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
}
