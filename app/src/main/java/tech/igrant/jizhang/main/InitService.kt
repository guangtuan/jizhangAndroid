package tech.igrant.jizhang.main

import io.reactivex.rxjava3.core.Observable
import tech.igrant.jizhang.main.account.AccountService
import tech.igrant.jizhang.main.subject.SubjectService

class InitService {

    companion object {
        fun init(): Observable<Unit> {
            return Observable.zip(
                listOf(
                    SubjectService.loadSubject(),
                    AccountService.loadAccount(),
                )
            ) { (_, _) -> }
        }
    }

}