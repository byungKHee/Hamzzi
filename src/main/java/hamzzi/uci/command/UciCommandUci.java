package hamzzi.uci.command;

import hamzzi.uci.core.EngineContext;

/**
 * 'uci' 명령어에 대응하는 클래스
 * 1. The engine must first respond with either one or two invocations of the id command.
 * 2. Then, the engine must send a series of zero or more invocations of the option command.
 * 3. Finally, the engine must indicate that it has finished sending option commands by sending the uciok command.
 */
public class UciCommandUci implements UciCommand {

    @Override
    public void execute(EngineContext context, String args){
        // 1. id command: 엔진 정보 보고 (최소 1번 이상)
        context.send("id name Hamzzi");
        context.send("id author byungKHee");

        // TODO
        // 2. option 명세 전달

        // 3. uciok: 모든 설정 보고가 끝났음을 알림
        context.send("uciok");

        context.log("UCI initialization complete.");
    }

}
