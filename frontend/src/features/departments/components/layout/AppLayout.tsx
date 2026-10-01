import { NavLink, Outlet } from 'react-router-dom'

export function AppLayout() {
    return (
        <div className="min-h-screen bg-slate-100">
            <div className="flex min-h-screen">
                <aside className="w-64 border-r border-slate-200 bg-white">
                    <div className="border-b border-slate-200 px-6 py-5">
                        <h1 className="text-xl font-bold text-slate-900">
                            Lebane
                        </h1>
                    </div>

                    <nav className="p-4">
                        <NavLink
                            to="/departamentos"
                            className={({ isActive }) =>
                                [
                                    'block rounded-lg px-4 py-3 text-sm font-medium transition',
                                    isActive
                                        ? 'bg-blue-50 text-blue-700'
                                        : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
                                ].join(' ')
                            }
                        >
                            Departamentos
                        </NavLink>
                    </nav>
                </aside>

                <main className="flex-1">
                    <div className="mx-auto max-w-7xl p-8">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    )
}