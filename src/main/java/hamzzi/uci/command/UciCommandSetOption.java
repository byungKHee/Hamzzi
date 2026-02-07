package hamzzi.uci.command;

import hamzzi.uci.core.EngineContext;

/**
 * 'setoption' 명령어 처리.
 * UCI GUI에서 전달한 옵션 값을 엔진 컨텍스트에 반영합니다.
 */
public class UciCommandSetOption implements UciCommand {
    @Override
    public void execute(EngineContext context, String args) {
        String[] tokens = args.trim().split("\\s+");
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        boolean nameMode = false;
        boolean valueMode = false;

        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            if ("name".equals(token)) {
                nameMode = true;
                valueMode = false;
                continue;
            }
            if ("value".equals(token)) {
                valueMode = true;
                nameMode = false;
                continue;
            }

            if (nameMode) {
                if (!nameBuilder.isEmpty()) nameBuilder.append(' ');
                nameBuilder.append(token);
            } else if (valueMode) {
                if (!valueBuilder.isEmpty()) valueBuilder.append(' ');
                valueBuilder.append(token);
            }
        }

        String name = nameBuilder.toString().trim();
        if (name.isEmpty()) {
            context.log("setoption ignored: missing option name");
            return;
        }

        String value = valueBuilder.toString().trim();
        context.setOption(name, value);
    }
}
