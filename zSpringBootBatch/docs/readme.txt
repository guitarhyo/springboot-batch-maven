##  배포/실행 

# Maven Build Goal 필드창에 입력  
# -Pdev pom.xml profile 확인 및 리소스 추가
clean install -Pdev


# 리눅스 실행 / 실행로그 확인
cd /BATCH 해당경로
nohup java -jar BATCH-DEV.jar & > /dev/null
tail -f nohup.out

# 프로세스 확인
ps -ef | grep {jar명}



